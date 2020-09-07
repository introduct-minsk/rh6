package tech.introduct.mailbox.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tech.introduct.mailbox.dto.file.LoadedFileDto;
import tech.introduct.mailbox.persistence.domain.FileEntity;
import tech.introduct.mailbox.persistence.domain.MessageEntity;
import tech.introduct.mailbox.persistence.repository.FileRepository;
import tech.introduct.mailbox.persistence.repository.MessageRepository;
import tech.introduct.mailbox.service.FileStorage;
import tech.introduct.mailbox.web.handler.ErrorInfo;
import tech.introduct.mailbox.web.json.PageRequestDto;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping(path = "/admin/data/mailbox_db", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Secured("ROLE_DB_READ_WRITE")
public class AdminMailboxController {
    private final MessageRepository messageRepository;
    private final FileRepository fileRepository;
    private final FileStorage fileStorage;

    @PostMapping(path = "/files/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public FileEntity uploadFile(@RequestParam MultipartFile file) throws IOException {
        return fileStorage.save(new LoadedFileDto(null, file));
    }

    @GetMapping("/files/{id}")
    public ResponseEntity<Resource> loadFile(@PathVariable UUID id) {
        var file = fileRepository.findById(id)
                .orElseThrow(() -> new ErrorInfo("file_not_found").badRequest());
        return fileStorage.load(file).toResponse();
    }

    @GetMapping("/messages")
    public Page<MessageEntity> findMessage(@Valid PageRequestDto request) {
        return messageRepository.findAll(request.toPage()).map(MessageEntity::unproxy);
    }

    @GetMapping("/messages/{id}")
    public Optional<MessageEntity> findMessageById(@PathVariable UUID id) {
        return messageRepository.findById(id).map(MessageEntity::unproxy);
    }

    @PostMapping(path = "/messages", consumes = MediaType.APPLICATION_JSON_VALUE)
    public MessageEntity saveMassage(@RequestBody MessageEntity request) {
        return messageRepository.save(request).unproxy();
    }
}
