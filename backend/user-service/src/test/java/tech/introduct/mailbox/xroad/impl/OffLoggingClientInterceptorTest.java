package tech.introduct.mailbox.xroad.impl;

import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.ws.context.MessageContext;

import static org.mockito.Mockito.mock;

@SpringBootTest
@TestPropertySource(properties = "logging.level.tech.introduct.mailbox.xroad=info")
class OffLoggingClientInterceptorTest {
    private LoggingClientInterceptor interceptor = new LoggingClientInterceptor("test");

    @Test
    void handleRequestWithOffDebug_expectNotLogs() {
        interceptor.handleRequest(mock(MessageContext.class, Answers.RETURNS_MOCKS));
    }
}
