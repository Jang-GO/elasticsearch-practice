package ureca.practice.elasticsearchpractice;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ureca.practice.elasticsearchpractice.document.TransactionFeedDocument;
import ureca.practice.elasticsearchpractice.entity.TransactionFeed;
import ureca.practice.elasticsearchpractice.repository.TransactionFeedRepository;
import ureca.practice.elasticsearchpractice.repository.TransactionFeedSearchRepository;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DummyDataInit implements CommandLineRunner {

    private final TransactionFeedRepository feedRepository;
    private final TransactionFeedSearchRepository searchRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // 애플리케이션 재시작 시 데이터가 중복 생성되는 것을 방지
        if (feedRepository.count() > 0) {
            System.out.println("데이터가 이미 존재하므로 더미 데이터를 생성하지 않습니다.");
            return;
        }

        System.out.println("더미 데이터를 생성합니다...");

        List<TransactionFeed> feeds = List.of(
                // SKT 데이터
                TransactionFeed.builder().sellerId("데이터판매왕").telecomCompanyId("SKT").title("SKT 10GB 데이터 팝니다").content("SKT 데이터 10기가 저렴하게 팔아요.").salesPrice(18000L).salesDataAmount(10000).progress("SELLING").build(),
                TransactionFeed.builder().sellerId("데이터요정").telecomCompanyId("SKT").title("SKT 2기가 데이터 선물용").content("SKT 2GB 선물하실 분 구해요. 바로 거래 가능합니다.").salesPrice(3800L).salesDataAmount(2000).progress("SELLING").build(),
                TransactionFeed.builder().sellerId("빠른거래").telecomCompanyId("SKT").title("SKT 500MB 소량 데이터 급처").content("500메가 급하게 처분합니다. 1000원에 드려요").salesPrice(1000L).salesDataAmount(500).progress("SELLING").build(),
                TransactionFeed.builder().sellerId("데이터판매왕").telecomCompanyId("SKT").title("하하하하").content("거래 완료된 게시글입니다.").salesPrice(2000L).salesDataAmount(1000).progress("SOLD_OUT").build(),
                TransactionFeed.builder().sellerId("SKT만세").telecomCompanyId("SKT").title("SKT 데이터 5GB 예약중").content("5기가 예약되었습니다. 구매 확정 대기중").salesPrice(9500L).salesDataAmount(5000).progress("RESERVED").build(),

                // KT 데이터
                TransactionFeed.builder().sellerId("데이터천사").telecomCompanyId("KT").title("KT 5기가 데이터 선물하기").content("KT 사용자분께 5GB 데이터 팝니다. 연락주세요.").salesPrice(9000L).salesDataAmount(5000).progress("SELLING").build(),
                TransactionFeed.builder().sellerId("올레KT").telecomCompanyId("KT").title("KT 데이터 20기가 대용량 판매").content("KT 20GB 데이터 필요하신 분? 대용량 팝니다.").salesPrice(35000L).salesDataAmount(20000).progress("SELLING").build(),
                TransactionFeed.builder().sellerId("케이티사랑").telecomCompanyId("KT").title("KT 1GB 데이터 나눔해요").content("케이티 1기가 데이터 그냥 드립니다. (가격은 100원)").salesPrice(100L).salesDataAmount(1600).progress("SELLING").build(),
                TransactionFeed.builder().sellerId("데이터천사").telecomCompanyId("KT").title("KT 2기가 예약 완료").content("2GB 예약되었습니다.").salesPrice(3800L).salesDataAmount(2000).progress("RESERVED").build(),
                TransactionFeed.builder().sellerId("올레KT").telecomCompanyId("KT").title("KT 500MB 판매완료").content("소량 데이터 판매되었습니다.").salesPrice(1000L).salesDataAmount(500).progress("SOLD_OUT").build(),

                // LGU+ 데이터
                TransactionFeed.builder().sellerId("유플러스맨").telecomCompanyId("LGU").title("LG U+ 데이터 2기가 급처").content("엘지 유플러스 2GB 데이터 급하게 처분합니다.").salesPrice(3500L).salesDataAmount(2000).progress("SELLING").build(),
                TransactionFeed.builder().sellerId("LG팬").telecomCompanyId("LGU").title("LGU 데이터 1기가 팝니다").content("유플러스 1GB 데이터 저렴하게 팔아요").salesPrice(1900L).salesDataAmount(1000).progress("SELLING").build(),
                TransactionFeed.builder().sellerId("데이터부자").telecomCompanyId("LGU").title("LG U+ 10기가 데이터").content("엘지 10기가 데이터 팝니다. 넉넉해요").salesPrice(17500L).salesDataAmount(10000).progress("SELLING").build(),
                TransactionFeed.builder().sellerId("유플러스맨").telecomCompanyId("LGU").title("LGU 5기가 판매완료").content("유플 5GB 데이터 거래 끝!").salesPrice(9000L).salesDataAmount(5000).progress("SOLD_OUT").build(),
                TransactionFeed.builder().sellerId("LG팬").telecomCompanyId("LGU").title("LGU 1GB 예약중").content("예약된 유플러스 데이터입니다.").salesPrice(1900L).salesDataAmount(1000).progress("RESERVED").build(),

                // 기타
                TransactionFeed.builder().sellerId("데이터부자").telecomCompanyId("SKT").title("SKT 데이터 24기가 대용량").content("24GB를 한번에! 데이터 걱정 끝").salesPrice(40000L).salesDataAmount(24000).progress("SELLING").build(),
                TransactionFeed.builder().sellerId("데이터요정").telecomCompanyId("KT").title("KT 3기가 데이터").content("KT 3GB 데이터 팝니다.").salesPrice(5500L).salesDataAmount(3000).progress("SELLING").build(),
                TransactionFeed.builder().sellerId("빠른거래").telecomCompanyId("LGU").title("유플러스 500메가").content("LGU 500MB 소량 급처").salesPrice(900L).salesDataAmount(500).progress("SELLING").build(),
                TransactionFeed.builder().sellerId("SKT만세").telecomCompanyId("SKT").title("SKT 7기가 데이터").content("7GB 데이터 팝니다. SKT만 가능").salesPrice(13000L).salesDataAmount(7000).progress("SELLING").build(),
                TransactionFeed.builder().sellerId("케이티사랑").telecomCompanyId("KT").title("KT 데이터 15기가").content("15GB 대용량 데이터 팝니다. 케이티 유저만").salesPrice(28000L).salesDataAmount(15000).progress("SELLING").build()
        );

        // 1. DB에 저장
        List<TransactionFeed> savedFeeds = feedRepository.saveAll(feeds);

        // 2. Elasticsearch에 저장 (데이터 동기화)
        List<TransactionFeedDocument> documents = savedFeeds.stream()
                .map(TransactionFeedDocument::from)
                .collect(Collectors.toList());
        searchRepository.saveAll(documents);

        System.out.println("==============================================");
        System.out.println(savedFeeds.size() + "개의 더미 데이터가 성공적으로 로드되었습니다.");
        System.out.println("==============================================");
    }
}

