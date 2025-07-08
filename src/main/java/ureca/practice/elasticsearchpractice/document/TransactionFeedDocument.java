package ureca.practice.elasticsearchpractice.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.*;
import ureca.practice.elasticsearchpractice.entity.TransactionFeed;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "transaction_feeds") // Elasticsearch 인덱스 이름
@Setting(settingPath = "elasticsearch/nori-settings.json") // <<<--- 이 부분을 추가했습니다
public class TransactionFeedDocument {

    @Id
    private String id; // JPA 엔티티의 ID와 동일한 값을 사용

    // sellerId는 정확히 일치하는 검색(필터링)과 전문 검색 모두를 위해 설정
    @Field(type = FieldType.Keyword)
    private String sellerIdKeyword; // 필터링용

    @Field(type = FieldType.Text, analyzer = "nori")
    private String sellerIdText; // 전문 검색용

    // 제목: nori 분석기를 사용하여 형태소 분석
    @Field(type = FieldType.Text, analyzer = "nori")
    private String title;

    // 내용: nori 분석기를 사용하여 형태소 분석
    @Field(type = FieldType.Text, analyzer = "nori")
    private String content;

    // 통신사: 정확히 일치(Term) 검색을 위해 Keyword 타입으로 설정
    @Field(type = FieldType.Keyword)
    private String telecomCompanyId;

    @Field(type = FieldType.Text, analyzer = "nori")
    private String telecomCompanyText; // 검색용

    @Field(type = FieldType.Long)
    private Long salesPrice;

    @Field(type = FieldType.Integer)
    private Integer salesDataAmount;

    @Field(type = FieldType.Keyword)
    private String progress;

//    @Field(type = FieldType.Date)
@Field(type = FieldType.Date, format = {}, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSSSSSSS||uuuu-MM-dd'T'HH:mm:ss.SSS||uuuu-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    // JPA 엔티티를 Elasticsearch 문서로 변환하는 정적 메서드
    public static TransactionFeedDocument from(TransactionFeed entity) {
        return TransactionFeedDocument.builder()
                .id(entity.getTransactionFeedId())
                .sellerIdKeyword(entity.getSellerId())
                .sellerIdText(entity.getSellerId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .telecomCompanyId(entity.getTelecomCompanyId())
                .salesPrice(entity.getSalesPrice())
                .salesDataAmount(entity.getSalesDataAmount())
                .progress(entity.getProgress())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
