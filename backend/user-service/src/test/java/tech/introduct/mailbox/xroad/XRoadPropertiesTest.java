package tech.introduct.mailbox.xroad;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class XRoadPropertiesTest {

    @Autowired
    private XRoadProperties properties;

    @Test
    void givenInvalidDatabase_whenGetDatabase_thenExceptionThrown() {
        assertThrows(IllegalArgumentException.class, () -> {
            String invalidDatabase = RandomStringUtils.randomAlphanumeric(5);
            properties.getService(invalidDatabase);
        });
    }
}
