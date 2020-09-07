package tech.introduct.mailbox.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import tech.introduct.mailbox.dto.MailboxSourceRequest;

public interface SearchService {

    void save(MailboxSourceRequest dto);

    Page<String> findIds(String query, String owner, PageRequest page);
}
