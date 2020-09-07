package tech.introduct.mailbox.siva.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@Value
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class ValidationConclusion {
    Policy policy;
    String validationTime;
    String signatureForm;
    List<ValidationWarning> validationWarnings = Collections.emptyList();
    ValidatedDocument validatedDocument;
    String validationLevel;
    List<SignatureValidationData> signatures = Collections.emptyList();
    int validSignaturesCount;
    int signaturesCount;
    List<TimeStampTokenValidationData> timeStampTokens = Collections.emptyList();
}
