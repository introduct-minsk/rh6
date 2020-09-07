package tech.introduct.mailbox.dto;

import lombok.Value;

import javax.validation.constraints.NotNull;
import java.time.ZonedDateTime;

@Value
public class MailboxSourceRequest {
    @NotNull
    String id;
    @NotNull
    ZonedDateTime date;
    String sender;
    String receiver;
    String value;
}
