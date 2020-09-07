package tech.introduct.mailbox;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.InMemoryTokenStore;

@Configuration
public class TestConfiguration {

    @Bean
    @Primary
    public TokenStore inMemoryTokenStore() {
        return new InMemoryTokenStore();
    }
}
