package tech.introduct.mailbox.siva.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@Value
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class TimeStampTokenValidationData {
    Indication indication;
    String signedBy;
    String signedTime;
    List<Error> error = Collections.emptyList();
}
