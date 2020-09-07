package tech.introduct.mailbox.socket.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tech.introduct.mailbox.dto.MessageDto;
import tech.introduct.mailbox.service.MessageListener;
import tech.introduct.mailbox.socket.event.WebSocketEvent;

import java.security.Principal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Optional.ofNullable;

@Slf4j
public class MessageEventSocketHandler extends TextWebSocketHandler implements MessageListener {
    private final Map<String, List<WebSocketSession>> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        var userId = getUserId(session);
        log.debug("after connection established for {}", userId);
        sessions.computeIfAbsent(userId, s -> new LinkedList<>()).add(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        removeSession(session, getUserId(session));
    }

    private void removeSession(WebSocketSession session, String userId) {
        var list = sessions.computeIfAbsent(userId, s -> new LinkedList<>());
        list.remove(session);
        if (list.isEmpty()) {
            sessions.remove(userId);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        var userId = getUserId(session);
        log.debug("after connection closed for {}", userId);
        removeSession(session, userId);
    }

    @Override
    public void mailing(MessageDto message) {
        sessions.getOrDefault(message.getReceiver().getId(), List.of())
                .forEach(session -> mailing(message, session));
    }

    @SneakyThrows
    private void mailing(MessageDto message, WebSocketSession session) {
        if (session.isOpen()) {
            var json = objectMapper.writeValueAsString(new WebSocketEvent("MESSAGE_RECEIVED", message));
            session.sendMessage(new TextMessage(json));
        }
    }

    private String getUserId(WebSocketSession session) {
        return ofNullable(session.getPrincipal())
                .map(Principal::getName)
                .orElseThrow(() -> new AccessDeniedException("unauthorized"));
    }
}
