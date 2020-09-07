package tech.introduct.mailbox.net;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.server.Ssl;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.web.client.HttpServerErrorException.ServiceUnavailable;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SSLConfigTest {

    @Test
    void updateRemoteTokenServices() throws Exception {
        var ssl = new Ssl();
        ssl.setKeyStore("classpath:test.jks");
        ssl.setKeyStorePassword("secret");
        ssl.setKeyPassword("secret");
        var properties = new ServerProperties();
        properties.setSsl(ssl);
        var sslContext = SSLConfig.sslContext(properties);
        var sslProperties = new SSLProperties();
        sslProperties.setCheck(true);
        var verifier = SSLConfig.noopHostnameVerifier(sslProperties);
        var mock = mock(RemoteTokenServices.class);
        new SSLConfig().updateRemoteTokenServices(mock, Optional.of(sslContext), verifier);
        verify(mock).setRestTemplate(argThat(argument -> {
            var handler = ((RestTemplate) argument).getErrorHandler();
            try {
                handler.handleError(new MockClientHttpResponse(new byte[]{}, HttpStatus.BAD_REQUEST));
            } catch (IOException e) {
                fail("has error");
            }
            assertThrows(ServiceUnavailable.class,
                    () -> handler.handleError(new MockClientHttpResponse(new byte[]{}, HttpStatus.SERVICE_UNAVAILABLE)));
            return true;
        }));
    }
}
