package ureca.practice.elasticsearchpractice.repository;


import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import ureca.practice.elasticsearchpractice.document.TransactionFeedDocument;

public interface TransactionFeedSearchRepository extends ElasticsearchRepository<TransactionFeedDocument, String> {
}
