package ureca.practice.elasticsearchpractice.service;

import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
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

    // --- 최종 완성된 검색 로직 ---
    @Transactional(readOnly = true)
    public Page<FeedDto.Response> unifiedSearch(String rawQuery, Pageable pageable) {

        if (!StringUtils.hasText(rawQuery)) {
            return Page.empty(pageable);
        }

        // 1) "5gb, 500mb" 패턴 추출 → 쿼리 문자열에서 제거
        Matcher m = DATA_PATTERN.matcher(rawQuery);
        Long dataSizeMB = null;
        if (m.find()) {
            long size = Long.parseLong(m.group(1));
            String unit = m.group(2).toLowerCase();
            if (unit.matches("gb|기가")) {
                dataSizeMB = size * 1000L;
            } else {
                dataSizeMB = size;
            }
//            rawQuery = m.replaceAll("").trim();
        }
        final String queryText = rawQuery;

        // 여러 조건을 조합하기 위해 BoolQuery.Builder를 사용합니다.
        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();


        // 1. 텍스트 필드에 대한 multi_match 쿼리 (항상 실행)
        // 'should'는 OR 조건과 유사하게 동작하며, 많이 일치할수록 점수가 높아집니다.
        boolQueryBuilder.should(s -> s
                .multiMatch(mm -> mm
                        .query(queryText)
                        .fields("title^3", "content", "sellerIdText", "telecomCompanyText") // 제목에는 3배의 가중치 부여
                        .type(TextQueryType.BestFields)
                )
        );
        log.info("👽👽텍스트");

        if (dataSizeMB != null) {
            long filterValue = dataSizeMB;
            boolQueryBuilder.filter(f -> f.term(t -> t
                    .field("salesDataAmount")
                    .value(filterValue)
                    .boost(2.0f)
            ));

            log.info("👽👽숫자");

        }

        // 3. 최종 쿼리 빌드
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q.bool(boolQueryBuilder.build())) // bool 쿼리 적용
                .withPageable(pageable)
                .withSort(Sort.by(Sort.Direction.DESC, "_score")) // 1. 정확도 순
                .withSort(Sort.by(Sort.Direction.DESC, "createdAt")) // 2. 최신순
                .build();

        // Elasticsearch 검색 실행
        SearchHits<TransactionFeedDocument> searchHits = elasticsearchOperations.search(query, TransactionFeedDocument.class);

        // Page 객체로 변환하여 반환
        Page<TransactionFeedDocument> page = org.springframework.data.support.PageableExecutionUtils.getPage(
                searchHits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList()),
                pageable,
                searchHits::getTotalHits
        );

        return page.map(FeedDto.Response::from);
    }
}
