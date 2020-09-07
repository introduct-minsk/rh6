package tech.introduct.mailbox.persistence.domain;

import org.junit.jupiter.api.Test;

public class JpaModelGenTest {

    @Test
    void generated() {
        new MessageEntity_() {
        };
        new BodyEntity_() {
        };
        new FileEntity_() {
        };
    }
}
