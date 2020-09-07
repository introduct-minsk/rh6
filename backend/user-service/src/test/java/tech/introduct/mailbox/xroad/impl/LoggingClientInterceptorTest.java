package tech.introduct.mailbox.xroad.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.ws.context.MessageContext;
import org.springframework.ws.soap.saaj.SaajSoapMessage;

import java.io.IOException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@TestPropertySource(properties = "logging.level.tech.introduct.mailbox.xroad=debug")
class LoggingClientInterceptorTest {
    private LoggingClientInterceptor interceptor = new LoggingClientInterceptor("test");

    @Test
    void handleRequest() {
        var message = mock(SaajSoapMessage.class);
        var context = mock(MessageContext.class);
        when(context.getRequest()).thenReturn(message);
        interceptor.handleRequest(context);
    }

    @Test
    void handleRequestWithError() throws Exception {
        var message = mock(SaajSoapMessage.class);
        doThrow(IOException.class).when(message).writeTo(any());
        var context = mock(MessageContext.class);
        when(context.getRequest()).thenReturn(message);
        interceptor.handleRequest(context);
    }

    @Test
    void handleRequestWithNotSaajSoapMessage_expectNotLogs() {
        interceptor.handleRequest(mock(MessageContext.class, Answers.RETURNS_MOCKS));
    }
}
