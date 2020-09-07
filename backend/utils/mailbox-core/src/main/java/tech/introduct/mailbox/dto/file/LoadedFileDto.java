package tech.introduct.mailbox.dto.file;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.Serializable;
import java.util.UUID;

import static java.util.Optional.ofNullable;

@Value
@EqualsAndHashCode(callSuper = true)
public class LoadedFileDto extends FileDto implements Serializable {
    @JsonIgnore
    MediaType type;
    @JsonIgnore
    byte[] bytes;

    public LoadedFileDto(UUID id, String name, MediaType type, byte[] bytes) {
        super(id, name);
        this.type = type;
        this.bytes = bytes;
    }

    public LoadedFileDto(MultipartFile file) throws IOException {
        this(UUID.randomUUID(), file);
    }

    public LoadedFileDto(UUID id, MultipartFile file) throws IOException {
        this(
                id,
                ofNullable(file.getOriginalFilename())
                        .orElse(file.getName()),
                ofNullable(file.getContentType())
                        .map(MediaType::valueOf)
                        .orElse(MediaType.APPLICATION_OCTET_STREAM),
                file.getBytes()
        );
    }

    public ResponseEntity<Resource> toResponse() {
        return ResponseEntity.ok()
                .contentType(type)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + getName() + "\"")
                .body(new ByteArrayResource(bytes));
    }
}
