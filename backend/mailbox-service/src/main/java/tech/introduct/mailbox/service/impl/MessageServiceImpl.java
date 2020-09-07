package tech.introduct.mailbox.service.impl;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.digidoc4j.impl.asic.asice.bdoc.BDocContainerBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import tech.introduct.mailbox.client.SearchClient;
import tech.introduct.mailbox.client.UserClient;
import tech.introduct.mailbox.dto.MessageDirection;
import tech.introduct.mailbox.dto.MessageDto;
import tech.introduct.mailbox.dto.MessageType;
import tech.introduct.mailbox.dto.UserDto;
import tech.introduct.mailbox.dto.draft.DraftMessage;
import tech.introduct.mailbox.dto.file.FileDto;
import tech.introduct.mailbox.dto.file.LoadedFileDto;
import tech.introduct.mailbox.dto.sign.SignatureDto;
import tech.introduct.mailbox.dto.user.UserSessionData;
import tech.introduct.mailbox.persistence.domain.BodyEntity;
import tech.introduct.mailbox.persistence.domain.MessageEntity;
import tech.introduct.mailbox.persistence.domain.MessageSpecification;
import tech.introduct.mailbox.persistence.repository.MessageRepository;
import tech.introduct.mailbox.service.*;
import tech.introduct.mailbox.web.handler.ErrorInfo;

import javax.transaction.Transactional;
import java.io.ByteArrayInputStream;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.getDigits;
import static tech.introduct.mailbox.dto.NotificationType.READ;
import static tech.introduct.mailbox.utils.EstonianIdUtils.addEEIfValid;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {
    private final MessageRepository messageRepository;
    private final UserClient userClient;
    private final SearchClient searchClient;
    private final List<MessageListener> messageListeners;
    private final SignValidator signValidator;
    private final FileStorage fileStorage;
    private final MessageDataFileConverter dataFileConverter;
    private final UserSessionData userData;

    @Override
    @Transactional
    public MessageDto send(AuthenticatedPrincipal user, DraftMessage draft) {
        var receiver = draft.getReceiver();
        if (StringUtils.isEmpty(receiver)) {
            throw new ErrorInfo("empty", "receiver").badRequest();
        }
        var message = MessageEntity.builder()
                .type(MessageType.SIMPLE)
                .senderUserId(user.getName())
                .sender(userData.requiredCurrentRoleId())
                .receiver(addEEIfValid(receiver))
                .subject(draft.getSubject())
                .text(draft.isSigned() ? null : draft.getText())
                .sign(draft.getSign().map(fileStorage::save).orElse(null))
                .attachments(draft.getAttachments().stream().map(fileStorage::save).collect(Collectors.toSet()))
                .build();
        MessageDto dto = map(messageRepository.save(message), true);
        searchClient.createSource(map(dto, dto.getText()));
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            public void afterCommit() {
                messageListeners.forEach(listener -> listener.mailing(dto));
            }
        });
        return dto;
    }

    @Override
    public Page<MessageDto> find(AuthenticatedPrincipal user, MessageDirection direction, PageRequest pageable) {
        var roleId = userData.requiredCurrentRoleId();
        var page = messageRepository.findAll(new MessageSpecification(roleId, direction), pageable);
        return map(page);
    }

    @Override
    public Page<MessageDto> search(AuthenticatedPrincipal user, String query, PageRequest pageable) {
        var page = searchClient.findSourceIds(userData.requiredCurrentRoleId(), query, pageable);
        var ids = page.stream().collect(Collectors.toList());
        var messages = messageRepository.findAllById(ids)
                .collect(Collectors.toMap(MessageEntity::getId, Function.identity()));
        return map(page.map(messages::get));
    }

    private Page<MessageDto> map(Page<MessageEntity> page) {
        var ids = page.stream().flatMap(message -> getUserIds(message).stream()).collect(Collectors.toSet());
        Map<String, UserDto> detailsMap = ids.isEmpty() ? Map.of() : userClient.getUserDetailsMap(ids);
        return page.map(message -> map(message, detailsMap, false));
    }

    private ArrayList<String> getUserIds(MessageEntity message) {
        var ids = Lists.newArrayList(message.getSenderUserId(), message.getReceiver());
        if (message.getRelated() != null) {
            ids.add(message.getRelated().getSenderUserId());
            ids.add(message.getRelated().getReceiver());
        }
        return ids;
    }

    @Override
    @Transactional
    public MessageDto get(AuthenticatedPrincipal user, UUID messageId) {
        var roleId = userData.requiredCurrentRoleId();
        var message = getWithAccessCheck(messageId);
        if (roleId.equals(message.getReceiver()) && message.isUnread()) {
            message.setUnread(false);
            if (message.getType() != MessageType.NOTIFICATION) {
                var notification = messageRepository.save(MessageEntity.toNotification(READ, message, user.getName()));
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    public void afterCommit() {
                        messageListeners.forEach(listener -> listener.mailing(map(notification, false)));
                    }
                });
            }
        }
        return map(message, true);
    }

    private MessageEntity getWithAccessCheck(UUID messageId) {
        var roleId = userData.requiredCurrentRoleId();
        return messageRepository.findById(messageId)
                .filter(m -> roleId.equals(m.getSender()) || roleId.equals(m.getReceiver()))
                .orElseThrow(() -> new ErrorInfo("not_found", "messageId").badRequest());
    }

    @Override
    public LoadedFileDto loadSign(AuthenticatedPrincipal user, UUID messageId) {
        var message = getWithAccessCheck(messageId);
        var sign = message.getSign();
        if (sign == null) {
            throw new ErrorInfo("message_not_signed").badRequest();
        }
        return fileStorage.load(sign);
    }

    @Override
    public LoadedFileDto loadAttachment(AuthenticatedPrincipal user, UUID messageId, UUID attachmentsId) {
        var message = getWithAccessCheck(messageId);
        var attachment = message.getAttachments().stream()
                .filter(entity -> entity.getId().equals(attachmentsId))
                .findFirst()
                .orElseThrow(() -> new ErrorInfo("not_found", "attachmentId").badRequest());
        return fileStorage.load(attachment);
    }

    private MessageDto map(MessageEntity message, boolean includeDetails) {
        var userDetails = userClient.getUserDetailsMap(getUserIds(message));
        return map(message, userDetails, includeDetails);
    }

    private MessageDto map(MessageEntity message, Map<String, UserDto> userMap, boolean details) {
        return MessageDto.builder()
                .id(message.getId())
                .sender(userMap.get(message.getSenderUserId()).withRoleId(message.getSender()))
                .receiver(userMap.get(message.getReceiver()))
                .type(message.getType())
                .subject(message.getSubject())
                .unread(message.isUnread())
                .createdOn(message.getCreatedOn())
                .related(ofNullable(message.getRelated()).map(e -> map(e, userMap, false)).orElse(null))
                .text(details ? getText(message) : null)
                .signature(details ? mapSignature(message) : null)
                .attachments(details ? mapAttachments(message) : Set.of())
                .build();
    }

    private String getText(MessageEntity message) {
        if (message.getSign() != null) {
            var dataFiles = BDocContainerBuilder.aContainer()
                    .fromStream(new ByteArrayInputStream(fileStorage.load(message.getSign()).getBytes()))
                    .build().getDataFiles();
            return dataFileConverter.getMessageBody(dataFiles);
        }
        return ofNullable(message.getBody()).map(BodyEntity::getText).orElse(null);
    }

    private Set<FileDto> mapAttachments(MessageEntity message) {
        return message.getAttachments().stream()
                .map(fileEntity -> new FileDto(fileEntity.getId(), fileEntity.getName()))
                .collect(Collectors.toSet());
    }

    private SignatureDto mapSignature(MessageEntity message) {
        return ofNullable(message.getSign())
                .map(sign -> signValidator.validate(sign.getName(), fileStorage.load(sign).getBytes()))
                .orElse(null);
    }

    private SearchClient.SourceRequest map(MessageDto message, String body) {
        var messageId = message.getId();
        var senderId = message.getSender().getId();
        var receiverId = message.getReceiver().getId();
        var data = Stream.of(
                message.getSubject(),
                body,
                message.getCreatedOn().getYear(),
                message.getCreatedOn().getMonth(),
                message.getCreatedOn().toLocalDate().format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
                senderId,
                getDigits(senderId),
                message.getSender().getFirstName(),
                message.getSender().getLastName(),
                message.getSender().getRoleId(),
                receiverId,
                getDigits(receiverId),
                message.getReceiver().getFirstName(),
                message.getReceiver().getLastName(),
                message.getReceiver().getRoleId(),
                message.getAttachments().stream().map(FileDto::getName).collect(Collectors.joining(" "))
        )
                .filter(Objects::nonNull)
                .map(Object::toString)
                .distinct()
                .collect(Collectors.joining(" "));
        return new SearchClient.SourceRequest(messageId, message.getCreatedOn(), senderId, receiverId, data);
    }
}
