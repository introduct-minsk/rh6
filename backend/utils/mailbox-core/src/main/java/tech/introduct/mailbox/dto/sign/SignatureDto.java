package tech.introduct.mailbox.dto.sign;

import lombok.Value;

import java.io.Serializable;
import java.time.ZonedDateTime;

@Value
public class SignatureDto implements Serializable {
    Boolean valid;
    String signedBy;
    ZonedDateTime signingTime;
}
