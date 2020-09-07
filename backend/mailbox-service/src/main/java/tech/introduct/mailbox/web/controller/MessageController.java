package tech.introduct.mailbox.web.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.introduct.mailbox.dto.MessageDirection;
import tech.introduct.mailbox.dto.MessageDto;
import tech.introduct.mailbox.service.MessageService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/messages", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping
    public Page<MessageDto> find(@AuthenticationPrincipal OAuth2User user, @Valid FindMessageRequest request) {
        var page = PageRequest.of(request.page, request.size);
        return messageService.find(user, request.direction, page);
    }

    @GetMapping("/{messageId}")
    public MessageDto get(@AuthenticationPrincipal OAuth2User user, @PathVariable UUID messageId) {
        return messageService.get(user, messageId);
    }

    @GetMapping("/search")
    public Page<MessageDto> find(@AuthenticationPrincipal OAuth2User user, @Valid SearchMessageRequest request) {
        var page = PageRequest.of(request.page, request.size);
        return messageService.search(user, request.query, page);
    }

    @GetMapping("/{messageId}/attachments/{attachmentsId}")
    public ResponseEntity<Resource> downloadAttachment(@AuthenticationPrincipal OAuth2User user,
                                                       @PathVariable UUID messageId, @PathVariable UUID attachmentsId) {
        return messageService.loadAttachment(user, messageId, attachmentsId).toResponse();
    }

    @GetMapping("/{messageId}/sign")
    public ResponseEntity<Resource> downloadSign(@AuthenticationPrincipal OAuth2User user,
                                                 @PathVariable UUID messageId) {
        return messageService.loadSign(user, messageId).toResponse();
    }

    @Data
    private static class FindMessageRequest {
        private MessageDirection direction;
        @Min(0)
        private int page = 0;
        @Min(5)
        @Max(50)
        private int size = 10;
    }

    @Data
    private static class SearchMessageRequest {
        @NotNull
        private String query;
        @Min(0)
        private int page = 0;
        @Min(5)
        @Max(50)
        private int size = 10;
    }
}
