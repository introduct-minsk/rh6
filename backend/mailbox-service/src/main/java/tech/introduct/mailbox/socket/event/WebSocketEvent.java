package tech.introduct.mailbox.socket.event;

import lombok.Value;

@Value
public class WebSocketEvent {
    String type;
    Object payload;
}
