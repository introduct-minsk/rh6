package tech.introduct.mailbox.session;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.server.Ssl;
import org.springframework.data.redis.connection.lettuce.LettuceClientConfiguration;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;

import java.time.Duration;

import static org.mockito.Mockito.mock;

class RedisHttpSessionConfigTest {

    @Test
    void correctCreation() throws Exception {
        var config = new RedisHttpSessionConfig();
        var ssl = new Ssl();
        ssl.setKeyStore("classpath:test.jks");
        ssl.setKeyStorePassword("secret");
        var properties = new ServerProperties();
        properties.setSsl(ssl);
        config.lettuceCustomizer(properties).customize(LettuceClientConfiguration.builder());
        var sessionProperties = new SessionProperties();
        sessionProperties.setTimeout(Duration.ZERO);
        config.setDefaultMaxInactiveInterval(sessionProperties, mock(RedisIndexedSessionRepository.class));
    }
}
