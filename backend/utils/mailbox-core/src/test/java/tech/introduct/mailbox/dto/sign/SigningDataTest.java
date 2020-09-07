package tech.introduct.mailbox.dto.sign;

import org.junit.jupiter.api.Test;
import tech.introduct.mailbox.web.handler.ErrorInfoRuntimeException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class SigningDataTest {

    @Test
    void getCertificateWithNull_expectException() {
        assertThrows(ErrorInfoRuntimeException.class, () -> new SigningData("wrong").getCertificate());
    }
}
