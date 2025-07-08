package ureca.practice.elasticsearchpractice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) // Auditing 기능 포함
@Table(name = "transaction_feed")
public class TransactionFeed {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // ID를 UUID로 자동 생성
    @Column(name = "transaction_feed_id")
    private String transactionFeedId;

    @Column(name = "seller_id", nullable = false)
    private String sellerId; // 판매자 닉네임으로 사용

    @Column(name = "telecom_company_id", nullable = false)
    private String telecomCompanyId;

    @Column(name = "title")
    private String title;

    @Column(name = "content", length = 1000)
    private String content;

    @Column(name = "sales_price")
    private Long salesPrice;

    @Column(name = "sales_data_amount")
    private Integer salesDataAmount; // 데이터 양 (MB 단위라고 가정)

    @Column(name = "progress")
    @Builder.Default
    private String progress = "SELLING"; // 판매중, RESERVED, SOLD_OUT

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted")
    @Builder.Default
    private boolean isDeleted = false;

    // 엔티티 수정 메서드
    public void update(String title, String content, Long salesPrice, Integer salesDataAmount, String progress) {
        this.title = title;
        this.content = content;
        this.salesPrice = salesPrice;
        this.salesDataAmount = salesDataAmount;
        this.progress = progress;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
