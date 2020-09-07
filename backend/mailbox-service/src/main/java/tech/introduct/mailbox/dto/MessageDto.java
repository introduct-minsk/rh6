package tech.introduct.mailbox.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Singular;
import lombok.Value;
import tech.introduct.mailbox.dto.file.FileDto;
import tech.introduct.mailbox.dto.sign.SignatureDto;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

@Value
@Builder(toBuilder = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MessageDto {
    UUID id;
    UserDto sender;
    UserDto receiver;
    MessageType type;
    String subject;
    boolean unread;
    ZonedDateTime createdOn;
    MessageDto related;
    String text;
    @Singular
    Set<? extends FileDto> attachments;
    SignatureDto signature;

    @JsonProperty("attachments")
    public Set<? extends FileDto> getNullableAttachments() {
        return attachments.isEmpty() ? null : attachments;
    }
}
