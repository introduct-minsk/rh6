package tech.introduct.mailbox.elasticsearch.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import tech.introduct.mailbox.elasticsearch.domain.MailboxSource;

public interface SourceRepository extends ElasticsearchRepository<MailboxSource, String> {
}
