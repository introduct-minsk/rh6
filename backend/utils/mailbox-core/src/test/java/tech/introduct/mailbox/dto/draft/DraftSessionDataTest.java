package tech.introduct.mailbox.dto.draft;

import org.junit.jupiter.api.Test;
import tech.introduct.mailbox.web.handler.ErrorInfoRuntimeException;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;

class DraftSessionDataTest {

    @Test
    void whenGetAttachmentWithRandomId_expectException() {
        var data = new DraftSessionData();
        data.create(null);
        assertThrows(ErrorInfoRuntimeException.class, () -> data.getAttachment(UUID.randomUUID()));
    }
}
