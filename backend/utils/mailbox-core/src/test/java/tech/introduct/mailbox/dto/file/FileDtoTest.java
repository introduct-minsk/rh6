package tech.introduct.mailbox.dto.file;

import org.junit.jupiter.api.Test;
import tech.introduct.mailbox.web.handler.ErrorInfoRuntimeException;

import java.util.UUID;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileDtoTest {

    @Test
    void constructWithExceptions() {
        assertThrows(ErrorInfoRuntimeException.class, () -> new FileDto(UUID.randomUUID(), null));
        assertThrows(ErrorInfoRuntimeException.class, () -> new FileDto(UUID.randomUUID(), random(256)));
    }
}
