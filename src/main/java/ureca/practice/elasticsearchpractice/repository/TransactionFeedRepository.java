package ureca.practice.elasticsearchpractice.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import ureca.practice.elasticsearchpractice.entity.TransactionFeed;

import java.util.Optional;

public interface TransactionFeedRepository extends JpaRepository<TransactionFeed, String> {
    // 삭제되지 않은 게시글만 조회
    Optional<TransactionFeed> findByTransactionFeedIdAndIsDeletedFalse(String id);
}
