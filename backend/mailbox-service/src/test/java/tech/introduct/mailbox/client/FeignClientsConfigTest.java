package tech.introduct.mailbox.client;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.server.Ssl;
import tech.introduct.mailbox.net.SSLConfig;
import tech.introduct.mailbox.net.SSLProperties;

import java.util.Optional;

class FeignClientsConfigTest {

    @Test
    void correctCreation_whenSslEnabled() throws Exception {
        var ssl = new Ssl();
        ssl.setKeyStore("classpath:test.jks");
        ssl.setKeyStorePassword("secret");
        ssl.setKeyPassword("secret");
        var properties = new ServerProperties();
        properties.setSsl(ssl);
        var sslContext = SSLConfig.sslContext(properties);
        var verifier = SSLConfig.noopHostnameVerifier(new SSLProperties());

        var config = new FeignClientsConfig();
        config.feignInterceptor(null, null, Optional.of(sslContext), verifier);
        config.sslFeignClient(Optional.of(sslContext), verifier);
    }
}