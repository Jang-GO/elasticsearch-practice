package ureca.practice.elasticsearchpractice.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch._types.query_dsl.TextQueryType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import ureca.practice.elasticsearchpractice.document.TransactionFeedDocument;
import ureca.practice.elasticsearchpractice.dto.FeedDto;
import ureca.practice.elasticsearchpractice.entity.TransactionFeed;
import ureca.practice.elasticsearchpractice.repository.TransactionFeedRepository;
import ureca.practice.elasticsearchpractice.repository.TransactionFeedSearchRepository;

import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TransactionFeedService {

    private final TransactionFeedRepository feedRepository;
    private final TransactionFeedSearchRepository searchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // "5gb", "5 GB", "5기가" 등을 모두 잡아내기 위한 정규식
    private static final Pattern DATA_PATTERN = Pattern.compile("(\\d+)\\s*(gb|기가|GB|mb|MB|메가)", Pattern.CASE_INSENSITIVE);


    // --- CRUD ---

    @Transactional
    public FeedDto.Response createFeed(FeedDto.Request request) {
        // 1. DB에 저장
        TransactionFeed savedFeed = feedRepository.save(request.toEntity());

        // 2. Elasticsearch에 저장 (데이터 동기화)
        searchRepository.save(TransactionFeedDocument.from(savedFeed));

        return FeedDto.Response.from(savedFeed);
    }

    @Transactional(readOnly = true)
    public FeedDto.Response getFeed(String id) {
        TransactionFeed feed = feedRepository.findByTransactionFeedIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + id));
        return FeedDto.Response.from(feed);
    }

    @Transactional
    public FeedDto.Response updateFeed(String id, FeedDto.Request request) {
        // 1. DB에서 엔티티 조회 및 수정
        TransactionFeed feed = feedRepository.findByTransactionFeedIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + id));
        feed.update(request.getTitle(), request.getContent(), request.getSalesPrice(), request.getSalesDataAmount(), request.getProgress());

        // 2. Elasticsearch 데이터 업데이트
        searchRepository.save(TransactionFeedDocument.from(feed));

        return FeedDto.Response.from(feed);
    }

    @Transactional
    public void deleteFeed(String id) {
        // 1. DB에서 논리적 삭제
        TransactionFeed feed = feedRepository.findByTransactionFeedIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("게시글을 찾을 수 없습니다. id=" + id));
        feed.delete();

        // 2. Elasticsearch에서 데이터 삭제
        searchRepository.deleteById(id);
    }

    // --- Search ---

    @Transactional(readOnly = true)
    public Page<FeedDto.Response> unifiedSearch(String rawQuery, Pageable pageable) {
        if (!StringUtils.hasText(rawQuery)) {
            return Page.empty(pageable);
        }

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();
        String[] words = rawQuery.trim().split("\\s+");

        // 검색할 통신사 목록
        final Set<String> TELECOM_COMPANIES = Set.of("KT", "SKT", "LGU");

        // === 단어가 1개일 때의 로직 수정 ===
        if (words.length == 1) {
            String word = words[0].toUpperCase();
            Matcher m = DATA_PATTERN.matcher(word);

            // 1. 검색어가 통신사 이름인 경우 (가장 먼저 확인)
            if (TELECOM_COMPANIES.contains(word)) {
                // telecomCompanyText.keyword 필드와 정확히 일치하는 것을 '반드시' 포함 (must)
                boolQueryBuilder.must(z -> z.term(t -> t
                        .field("telecomCompanyId") // .keyword 필드 사용
                        .value(word)
                ));
                log.info("✅ 통신사 필터링: {}", word);
            } else if (m.matches()) { // .find() 대신 .matches()로 단어 전체가 패턴에 일치하는지 확인
                long size = Long.parseLong(m.group(1));
                String unit = m.group(2).toLowerCase();
                long dataSizeMB = unit.matches("gb|기가") ? size * 1000L : size;

                // "데이터 크기"라는 개념을 하나의 bool 쿼리로 묶어 OR 조건을 명시적으로 구현
                BoolQuery.Builder dataQueryBuilder = new BoolQuery.Builder();

                // 조건 1: salesDataAmount 필드가 정확히 일치
                dataQueryBuilder.should(s -> s.term(t -> t
                        .field("salesDataAmount")
                        .value(dataSizeMB)
                ));

                // 조건 2: 제목 또는 내용에 "1기가" 텍스트가 포함
                dataQueryBuilder.should(s -> s.multiMatch(mm -> mm
                        .query(word)
                        .fields("title^2", "content") // 가중치 조절
                        .type(TextQueryType.Phrase) // Phrase 검색으로 정확도 향상 ("10기가" 등 제외)
                ));

                // 두 조건 중 하나 이상 만족해야 함
                dataQueryBuilder.minimumShouldMatch("1");

                // 이 "데이터 크기" 조건을 반드시 만족하도록 must 절에 추가
                boolQueryBuilder.must(y -> y.bool(dataQueryBuilder.build()));

            } else {
                // 데이터 크기 패턴이 아닌 일반 단어는 기존처럼 검색
                boolQueryBuilder.should(s -> s
                        .multiMatch(mm -> mm
                                .query(word)
                                .fields("title^3", "content", "sellerIdText", "telecomCompanyText")
                                .type(TextQueryType.BestFields)
                        )
                );
            }

        } else {
            // === 단어가 2개 이상일 때의 로직 (기존과 동일) ===
            for (String word : words) {
                Matcher m = DATA_PATTERN.matcher(word);
                if (m.matches()) {
                    long size = Long.parseLong(m.group(1));
                    String unit = m.group(2).toLowerCase();
                    long dataSizeMB = unit.matches("gb|기가") ? size * 1000L : size;

                    // 여기도 마찬가지로 OR 조건을 묶어주는 로직을 추가할 수 있습니다.
                    // 편의상 질문에 주신 코드의 구조를 유지했습니다.
                    boolQueryBuilder.must(s -> s.term(t -> t
                            .field("salesDataAmount")
                            .value(dataSizeMB)
                    ));
                } else {
                    boolQueryBuilder.must(s -> s
                            .multiMatch(mm -> mm
                                    .query(word)
                                    .fields("title^3", "content", "sellerIdText", "telecomCompanyText")
                                    .type(TextQueryType.BestFields)
                            )
                    );
                }
            }
        }

        // 최종 쿼리
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQueryBuilder.build()))
                .withPageable(pageable)
                .withSort(Sort.by(Sort.Direction.DESC, "_score"))
                .withSort(Sort.by(Sort.Direction.DESC, "createdAt"))
                .build();

        // ... (이하 동일)
        SearchHits<TransactionFeedDocument> searchHits = elasticsearchOperations.search(query, TransactionFeedDocument.class);
        Page<TransactionFeedDocument> page = org.springframework.data.support.PageableExecutionUtils.getPage(
                searchHits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList()),
                pageable,
                searchHits::getTotalHits
        );
        return page.map(FeedDto.Response::from);
    }
}