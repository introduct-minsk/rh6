package tech.introduct.mailbox.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import tech.introduct.mailbox.dto.MailboxSourceRequest;
import tech.introduct.mailbox.elasticsearch.domain.MailboxSource;
import tech.introduct.mailbox.elasticsearch.repository.SourceRepository;
import tech.introduct.mailbox.service.SearchService;
import tech.introduct.mailbox.web.handler.ErrorInfo;

import java.util.Date;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchServiceImpl implements SearchService {
    private final SourceRepository repository;

    @Override
    public void save(MailboxSourceRequest dto) {
        var source = new MailboxSource();
        source.setId(dto.getId());
        source.setDate(Date.from(dto.getDate().toInstant()));
        source.setSender(dto.getSender());
        source.setReceiver(dto.getReceiver());
        source.setValue(dto.getValue());
        log.debug("elasticsearch save {}", source);
        repository.save(source);
    }

    @Override
    public Page<String> findIds(String query, String role, PageRequest page) {
        log.debug("elasticsearch search {} {} {}", query, role, page);
        var ownerQuery = QueryBuilders.boolQuery()
                .should(QueryBuilders.queryStringQuery(role).defaultField("sender"))
                .should(QueryBuilders.queryStringQuery(role).defaultField("receiver"));
        var boolQuery = QueryBuilders.boolQuery()
                .must(ownerQuery)
                .must(QueryBuilders.queryStringQuery(query));
        try {
            var ids = repository.search(boolQuery, page).map(MailboxSource::getId);
            log.debug("elasticsearch search response {}", ids);
            return ids;
        } catch (ElasticsearchStatusException e) {
            log.info("elasticsearch request error", e);
            throw new ErrorInfo(e.getMessage()).exception(HttpStatus.resolve(e.status().getStatus()));
        }
    }
}
