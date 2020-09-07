package tech.introduct.mailbox.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.AuthenticatedPrincipal;
import tech.introduct.mailbox.dto.MessageDirection;
import tech.introduct.mailbox.dto.MessageDto;
import tech.introduct.mailbox.dto.draft.DraftMessage;
import tech.introduct.mailbox.dto.file.LoadedFileDto;

import java.util.UUID;

public interface MessageService {

    MessageDto send(AuthenticatedPrincipal user, DraftMessage draft);

    Page<MessageDto> find(AuthenticatedPrincipal user, MessageDirection direction, PageRequest page);

    Page<MessageDto> search(AuthenticatedPrincipal user, String query, PageRequest pageable);

    MessageDto get(AuthenticatedPrincipal user, UUID messageId);

    LoadedFileDto loadSign(AuthenticatedPrincipal user, UUID messageId);

    LoadedFileDto loadAttachment(AuthenticatedPrincipal user, UUID messageId, UUID attachmentsId);
}
