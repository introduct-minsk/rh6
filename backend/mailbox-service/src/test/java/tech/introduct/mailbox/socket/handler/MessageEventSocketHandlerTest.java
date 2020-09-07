package tech.introduct.mailbox.socket.handler;

import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.socket.WebSocketSession;
import tech.introduct.mailbox.dto.MessageDto;
import tech.introduct.mailbox.dto.UserDto;

import java.io.IOException;

import static org.apache.commons.lang3.RandomStringUtils.random;
import static org.apache.commons.lang3.RandomStringUtils.randomAlphabetic;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class MessageEventSocketHandlerTest {
    private MessageEventSocketHandler handler = new MessageEventSocketHandler();

    @Test
    void handleTransportError() {
        var userId = randomAlphabetic(10);
        var session = mock(WebSocketSession.class);
        when(session.getPrincipal()).thenReturn(() -> userId);
        handler.handleTransportError(session, new Exception());
        handler.afterConnectionEstablished(session);
        session = mock(WebSocketSession.class);
        when(session.getPrincipal()).thenReturn(() -> userId);
        handler.afterConnectionEstablished(session);
        handler.handleTransportError(session, new Exception());
    }

    @Test
    void accessDeniedWithoutUser() {
        var session = mock(WebSocketSession.class);
        assertThrows(AccessDeniedException.class, () -> handler.handleTransportError(session, new Exception()));
    }

    @Test
    void mailingWithCloseSession() {
        var userId = random(10);
        var session = mock(WebSocketSession.class);
        when(session.getPrincipal()).thenReturn(() -> userId);
        when(session.isOpen()).thenReturn(false);
        handler.afterConnectionEstablished(session);
        var message = MessageDto.builder()
                .receiver(new UserDto(userId, null, null, null))
                .build();
        handler.mailing(message);
    }

    @Test
    void mailingWithExceptionWhenSendMessage() throws IOException {
        var userId = random(10);
        var session = mock(WebSocketSession.class);
        when(session.getPrincipal()).thenReturn(() -> userId);
        when(session.isOpen()).thenReturn(true);
        doThrow(RuntimeException.class).when(session).sendMessage(any());
        handler.afterConnectionEstablished(session);
        var message = MessageDto.builder()
                .receiver(new UserDto(userId, null, null, null))
                .build();
        assertThrows(RuntimeException.class, () -> handler.mailing(message));
    }
}
