package tech.introduct.mailbox.siva.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidationRequest {
    FileType documentType;
    @NonNull
    String filename;
    @NonNull
    String document;
    String signaturePolicy;
    String reportType;
}
