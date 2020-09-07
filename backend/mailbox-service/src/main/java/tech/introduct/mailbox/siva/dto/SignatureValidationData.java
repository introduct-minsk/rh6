package tech.introduct.mailbox.siva.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Value;

import java.util.Collections;
import java.util.List;

@Value
@AllArgsConstructor(onConstructor = @__(@JsonCreator))
public class SignatureValidationData {
    String id;
    String signatureFormat;
    String signatureLevel;
    String signedBy;
    SubjectDistinguishedName subjectDistinguishedName;
    Indication indication;
    String subIndication;
    List<Error> errors = Collections.emptyList();
    List<SignatureScope> signatureScopes = Collections.emptyList();
    String claimedSigningTime;
    List<Warning> warnings = Collections.emptyList();
    Info info;
}
