package tech.introduct.mailbox.service;

import tech.introduct.mailbox.dto.MessageDto;

public interface MessageListener {

    void mailing(MessageDto message);
}
