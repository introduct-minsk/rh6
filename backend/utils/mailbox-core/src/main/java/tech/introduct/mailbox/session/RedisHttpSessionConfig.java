package tech.introduct.mailbox.session;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.SslOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.boot.autoconfigure.session.SessionProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;
import org.springframework.util.ResourceUtils;

@EnableRedisHttpSession
@ConditionalOnProperty(name = "spring.session.enable", matchIfMissing = true)
public class RedisHttpSessionConfig {

    @Autowired
    public void setDefaultMaxInactiveInterval(SessionProperties properties, RedisIndexedSessionRepository repository) {
        repository.setDefaultMaxInactiveInterval((int) properties.getTimeout().toSeconds());
    }

    @Bean
    @ConditionalOnProperty(name = "server.ssl.key-store")
    public static LettuceClientConfigurationBuilderCustomizer lettuceCustomizer(ServerProperties properties) throws Exception {
        var ssl = properties.getSsl();
        var keystore = ResourceUtils.getFile(ssl.getKeyStore()).getAbsoluteFile();
        SslOptions sslOptions = SslOptions.builder()
                .jdkSslProvider()
                .keystore(keystore, ssl.getKeyStorePassword().toCharArray())
                .build();
        ClientOptions clientOptions = ClientOptions.builder().sslOptions(sslOptions).build();
        return builder -> builder.clientOptions(clientOptions).useSsl().disablePeerVerification();
    }
}
