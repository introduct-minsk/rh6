package tech.introduct.mailbox.siva.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationResponseTest {

    @Test
    void deserialize() throws Exception {
        var json = new ClassPathResource("siva/sample-report-response.json").getInputStream();
        var response = new ObjectMapper().registerModule(new ParameterNamesModule()).readValue(json, ValidationResponse.class);
        ValidationConclusion conclusion = response.getValidationReport().getValidationConclusion();
        assertEquals("POLv4", conclusion.getPolicy().getPolicyName());
        assertEquals("ASiC-E", conclusion.getSignatureForm());
        assertEquals("ARCHIVAL_DATA", conclusion.getValidationLevel());
        assertEquals("QESIG", conclusion.getSignatures().get(0).getSignatureLevel());
        assertEquals(1, conclusion.getSignaturesCount());
        assertEquals(1, conclusion.getValidSignaturesCount());
    }
}
