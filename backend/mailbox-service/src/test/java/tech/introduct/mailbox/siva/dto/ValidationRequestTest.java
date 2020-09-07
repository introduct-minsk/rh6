package tech.introduct.mailbox.siva.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationRequestTest {

    @Test
    void correctSerialisation() throws Exception {
        var request = new ValidationRequest(FileType.ASICE, "name.txt", "document",
                "polisy", "report");
        var json = new ObjectMapper().writeValueAsString(request);
        var expectedJson = "{\"documentType\":\"ASICE\",\"filename\":\"name.txt\",\"document\":\"document\",\"signaturePolicy\":\"polisy\",\"reportType\":\"report\"}";
        assertEquals(expectedJson, json);
    }
}
