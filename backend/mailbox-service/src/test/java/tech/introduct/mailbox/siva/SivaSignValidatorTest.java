package tech.introduct.mailbox.siva;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import tech.introduct.mailbox.dto.sign.SignatureDto;

import java.util.Base64;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@SpringBootTest
class SivaSignValidatorTest {
    private SivaSignValidator validator;
    private MockRestServiceServer mockServer;
    @Autowired
    private RestTemplateBuilder restTemplateBuilder;


    @BeforeEach
    void setUp() {
        validator = new SivaSignValidator(new SivaProperties(), restTemplateBuilder);
        mockServer = MockRestServiceServer.createServer(validator.getRestTemplate());
    }

    @Test
    void validateWithOk() {
        assertTrue(mockAndValidate("siva/sample-report-response.json").getValid());
        mockServer.verify();
    }

    @Test
    void validateWhenNotValid() {
        assertFalse(mockAndValidate("siva/sample-report-response-not-valid.json").getValid());
        mockServer.verify();
    }

    @Test
    void validateWhenNoSignatureFound() {
        assertFalse(mockAndValidate("siva/sample-report-response-no-signature.json").getValid());
        mockServer.verify();
    }

    @Test
    void validateWithException() {
        mockServer.expect(requestTo("http://localhost:8080/validate"))
                .andRespond(withStatus(HttpStatus.BAD_GATEWAY));
        assertNull(validator.validate("test.txt", new byte[]{}).getValid());
        mockServer.verify();
    }

    private SignatureDto mockAndValidate(String path) {
        var bytes = randomAlphanumeric(10).getBytes();
        var filename = "test.txt";
        mockServer.expect(requestTo("http://localhost:8080/validate"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(jsonPath("$.filename", is(filename)))
                .andExpect(jsonPath("$.document", is(Base64.getEncoder().encodeToString(bytes))))
                .andRespond(withStatus(HttpStatus.OK)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ClassPathResource(path))
                );
        return validator.validate(filename, bytes);
    }
}
