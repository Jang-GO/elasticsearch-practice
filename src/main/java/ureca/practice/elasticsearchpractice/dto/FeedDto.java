package ureca.practice.elasticsearchpractice.dto;

import lombok.Builder;
import lombok.Data;
import ureca.practice.elasticsearchpractice.document.TransactionFeedDocument;
import ureca.practice.elasticsearchpractice.entity.TransactionFeed;

import java.time.LocalDateTime;

public class FeedDto {

    @Data
    public static class Request {
        private String sellerId;
        private String telecomCompanyId;
        private String title;
        private String content;
        private Long salesPrice;
        private Integer salesDataAmount;
        private String progress;

        public TransactionFeed toEntity() {
            return TransactionFeed.builder()
                    .sellerId(sellerId)
                    .telecomCompanyId(telecomCompanyId)
                    .title(title)
                    .content(content)
                    .salesPrice(salesPrice)
                    .salesDataAmount(salesDataAmount)
                    .progress(progress)
                    .build();
        }
    }

    @Data
    @Builder
    public static class Response {
        private String transactionFeedId;
        private String sellerId;
        private String telecomCompanyId;
        private String title;
        private String content;
        private Long salesPrice;
        private Integer salesDataAmount;
        private String progress;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Response from(TransactionFeed entity) {
            return Response.builder()
                    .transactionFeedId(entity.getTransactionFeedId())
                    .sellerId(entity.getSellerId())
                    .telecomCompanyId(entity.getTelecomCompanyId())
                    .title(entity.getTitle())
                    .content(entity.getContent())
                    .salesPrice(entity.getSalesPrice())
                    .salesDataAmount(entity.getSalesDataAmount())
                    .progress(entity.getProgress())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .build();
        }

        public static Response from(TransactionFeedDocument doc) {
            return Response.builder()
                    .transactionFeedId(doc.getId())
                    .sellerId(doc.getSellerIdKeyword())
                    .telecomCompanyId(doc.getTelecomCompanyId())
                    .title(doc.getTitle())
                    .content(doc.getContent())
                    .salesPrice(doc.getSalesPrice())
                    .salesDataAmount(doc.getSalesDataAmount())
                    .progress(doc.getProgress())
                    .createdAt(doc.getCreatedAt())
                    .build();
        }
    }
}
