package tech.introduct.mailbox.web.controller;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.introduct.mailbox.dto.MessageDto;
import tech.introduct.mailbox.dto.draft.DraftMessage;
import tech.introduct.mailbox.dto.file.LoadedFileDto;
import tech.introduct.mailbox.dto.sign.SignatureDto;
import tech.introduct.mailbox.properties.MailboxProperties;
import tech.introduct.mailbox.service.DraftMessageService;
import tech.introduct.mailbox.service.MessageService;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.UUID;

@RestController
@RequestMapping(path = "/api/v1/messages/draft", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class DraftMessageController {
    private final DraftMessageService draftMessageService;
    private final MessageService messageService;
    private final MailboxProperties mailboxProperties;
    private final MultipartProperties multipartProperties;

    @GetMapping
    public DraftMessage get() {
        return draftMessageService.get();
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public DraftMessage create(@RequestBody @Valid DraftMessage request) {
        return draftMessageService.create(request);
    }

    @PatchMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public DraftMessage update(@RequestBody @Valid DraftMessage request) {
        return draftMessageService.update(request);
    }

    @DeleteMapping
    public DraftMessage delete() {
        return draftMessageService.delete();
    }

    @PostMapping("/send")
    public MessageDto send(@AuthenticationPrincipal OAuth2User user) {
        var send = messageService.send(user, draftMessageService.get());
        draftMessageService.delete();
        return send;
    }

    @PostMapping(path = "/attachments/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LoadedFileDto uploadAttachment(@RequestParam MultipartFile file) {
        return draftMessageService.uploadAttachment(file);
    }

    @PutMapping(path = "/attachments/{attachmentsId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LoadedFileDto updateAttachment(@RequestParam MultipartFile file, @PathVariable UUID attachmentsId) {
        return draftMessageService.updateAttachment(attachmentsId, file);
    }

    @GetMapping("/attachments/{attachmentsId}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable UUID attachmentsId) {
        return draftMessageService.loadAttachment(attachmentsId).toResponse();
    }

    @DeleteMapping(path = "/attachments/{attachmentId}")
    public LoadedFileDto deleteAttachment(@PathVariable UUID attachmentId) {
        return draftMessageService.deleteAttachment(attachmentId);
    }

    @GetMapping("/sign/data")
    public SignDataResponse signData(@AuthenticationPrincipal OAuth2User user, @RequestParam String certInHex) {
        var hex = draftMessageService.getDataToSignInHex(user, certInHex);
        return new SignDataResponse(hex);
    }

    @PostMapping("/sign")
    public SignatureDto sign(@RequestBody @Valid SignMessageRequest request) {
        return draftMessageService.sign(request.signatureInHex);
    }

    @DeleteMapping("/sign")
    public void sign() {
        draftMessageService.deleteSign();
    }

    @GetMapping("/sign")
    public ResponseEntity<Resource> downloadSign() {
        return draftMessageService.loadSign().toResponse();
    }

    @GetMapping("/settings")
    public DraftMessageSettings getSettings() {
        var maxAttachmentNumber = mailboxProperties.getMaxAttachmentNumber();
        var maxFileSizeBytes = multipartProperties.getMaxFileSize().toBytes();
        return new DraftMessageSettings(maxAttachmentNumber, maxFileSizeBytes);
    }

    @Value
    private static class DraftMessageSettings {
        int maxAttachmentNumber;
        long maxFileSizeBytes;
    }

    @Value
    private static class SignDataResponse {
        String hex;
    }

    @Data
    private static class SignMessageRequest {
        @NotNull
        private String signatureInHex;
    }
}
