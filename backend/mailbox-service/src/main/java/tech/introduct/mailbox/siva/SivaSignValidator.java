package tech.introduct.mailbox.siva;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tech.introduct.mailbox.dto.sign.SignatureDto;
import tech.introduct.mailbox.service.SignValidator;
import tech.introduct.mailbox.siva.dto.SignatureValidationData;
import tech.introduct.mailbox.siva.dto.ValidationConclusion;
import tech.introduct.mailbox.siva.dto.ValidationRequest;
import tech.introduct.mailbox.siva.dto.ValidationResponse;

import java.time.ZonedDateTime;
import java.util.Base64;

import static java.util.Objects.requireNonNull;

@Service
@Slf4j
public class SivaSignValidator implements SignValidator {
    private final SivaProperties properties;
    @Getter(AccessLevel.PACKAGE)
    private final RestTemplate restTemplate;

    public SivaSignValidator(SivaProperties properties, RestTemplateBuilder restTemplateBuilder) {
        this.properties = properties;
        this.restTemplate = restTemplateBuilder
                .rootUri(properties.getServiceHost())
                .build();
    }

    public SignatureDto validate(String filename, byte[] bytes) {
        String encoded = Base64.getEncoder().encodeToString(bytes);
        try {
            var request = ValidationRequest.builder()
                    .document(encoded)
                    .filename(filename)
                    .build();
            var response = restTemplate.postForObject(properties.getValidatePath(), request, ValidationResponse.class);
            var conclusion = requireNonNull(response).getValidationReport().getValidationConclusion();
            log.debug("siva_validate request {} response {}", request, response);
            boolean valid = isValid(conclusion);
            if (!valid) {
                log.warn("siva not valid response {}", response);
            }
            return new SignatureDto(valid, getSignedBy(conclusion), getSigningTime(conclusion));
        } catch (Exception e) {
            log.error("siva_exception", e);
            return new SignatureDto(null, null, null);
        }
    }

    private boolean isValid(ValidationConclusion conclusion) {
        int validSignatureCount = conclusion.getValidSignaturesCount();
        int totalSignatureCount = conclusion.getSignaturesCount();
        return validSignatureCount == totalSignatureCount && totalSignatureCount > 0;
    }

    private String getSignedBy(ValidationConclusion conclusion) {
        return conclusion.getSignatures().stream()
                .map(SignatureValidationData::getSignedBy)
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .orElse(null);
    }

    private ZonedDateTime getSigningTime(ValidationConclusion conclusion) {
        return conclusion.getSignatures().stream()
                .map(SignatureValidationData::getClaimedSigningTime)
                .filter(StringUtils::isNotEmpty)
                .findFirst()
                .map(ZonedDateTime::parse)
                .orElse(null);
    }
}
