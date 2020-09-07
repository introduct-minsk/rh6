package tech.introduct.mailbox.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import tech.introduct.mailbox.OAuth2AuthenticationHelper;
import tech.introduct.mailbox.client.SearchClient;
import tech.introduct.mailbox.client.UserClient;
import tech.introduct.mailbox.dto.draft.DraftMessage;
import tech.introduct.mailbox.dto.user.UserRole;
import tech.introduct.mailbox.dto.user.UserSessionData;
import tech.introduct.mailbox.service.MessageService;

import javax.websocket.ContainerProvider;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;
import static org.junit.jupiter.api.Assertions.*;
import static tech.introduct.mailbox.utils.MockUtils.configureMock;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension.class)
@MockBean(SearchClient.class)
class MessageWebSocketTest {
    @LocalServerPort
    private int randomServerPort;
    @Autowired
    private MessageService messageService;
    @MockBean
    private UserClient userClient;
    @MockBean
    private UserSessionData userData;

    @BeforeEach
    void setUp() {
        configureMock(userClient);
        configureMock(userData);
    }

    @Test
    void whenSubscribeWebSocket_thenReceiveEvent() throws Exception {
        var senderId = randomNumeric(11);
        var clientId = randomNumeric(11);
        var result = new CompletableFuture<String>();

        var webSocketSession = subscribe(clientId, result, "ROLE_USER").get(10, TimeUnit.SECONDS);
        assertTrue(webSocketSession.isOpen());

        var subject = randomAlphanumeric(5);
        userData.setCurrentRole(new UserRole(senderId));
        messageService.send(() -> senderId, new DraftMessage(clientId, subject, randomAlphanumeric(50)));
        String json;
        try {
            json = result.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            e.printStackTrace();
            webSocketSession.close();
            return;
        }
        var jsonNode = new ObjectMapper().readTree(json);
        assertEquals("MESSAGE_RECEIVED", jsonNode.get("type").asText());
        assertEquals(senderId, jsonNode.get("payload").get("sender").get("id").asText());
        assertEquals(clientId, jsonNode.get("payload").get("receiver").get("id").asText());
        assertEquals(subject, jsonNode.get("payload").get("subject").asText());

        webSocketSession.close();
    }

    private ListenableFuture<WebSocketSession> subscribe(String clientId, CompletableFuture<String> result, String role) {
        var container = ContainerProvider.getWebSocketContainer();
        container.setDefaultMaxTextMessageBufferSize(1024 * 1024);
        var webSocketClient = new StandardWebSocketClient(container);
        var headers = new WebSocketHttpHeaders();
        headers.add(HttpHeaders.AUTHORIZATION, OAuth2AuthenticationHelper.bearer(clientId, role));
        return webSocketClient.doHandshake(new TextWebSocketHandler() {

            @Override
            protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                result.complete(message.getPayload());
            }
        }, headers, URI.create("ws://localhost:" + randomServerPort + "/websocket/messages/subscribe"));
    }
}
