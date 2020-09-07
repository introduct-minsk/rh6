package tech.introduct.mailbox.persistence.domain;

import org.junit.jupiter.api.Test;
import tech.introduct.mailbox.dto.MessageType;

import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

class MessageEntityTest {

    @Test
    void setBodyForHibernateWithNullBody_expectOk() {
        var messageEntity = builderWithRequired().build();
        messageEntity.setBody(null);
        messageEntity.setBody(new BodyEntity());
    }

    private MessageEntity.MessageEntityBuilder builderWithRequired() {
        return MessageEntity.builder()
                .type(MessageType.SIMPLE)
                .senderUserId(randomNumeric(11))
                .sender(randomNumeric(11))
                .receiver(randomNumeric(11));
    }
}
