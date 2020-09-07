package tech.introduct.mailbox.dto.user;

import org.junit.jupiter.api.Test;
import tech.introduct.mailbox.web.handler.ErrorInfoRuntimeException;

import static org.junit.jupiter.api.Assertions.assertThrows;

class UserSessionDataTest {

    @Test
    void requiredCurrentRoleId_exceptionCases() {
        assertThrows(ErrorInfoRuntimeException.class, () -> new UserSessionData().requiredCurrentRoleId());
        assertThrows(ErrorInfoRuntimeException.class, () -> {
            var data = new UserSessionData();
            data.setCurrentRole(new UserRole());
            data.requiredCurrentRoleId();
        });
    }
}
