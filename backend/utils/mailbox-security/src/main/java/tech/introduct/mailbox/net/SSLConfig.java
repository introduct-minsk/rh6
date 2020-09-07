package tech.introduct.mailbox.net;

import org.apache.http.conn.ssl.DefaultHostnameVerifier;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.conn.util.PublicSuffixMatcherLoader;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContextBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.util.Optional;

@Configuration
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class SSLConfig {

    @Autowired
    public void updateRemoteTokenServices(RemoteTokenServices remoteTokenServices, Optional<SSLContext> sslContext,
                                          HostnameVerifier verifier) {
        sslContext.ifPresent(context -> {
            var requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom()
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(context, verifier))
                    .build());
            var restTemplate = new RestTemplate(requestFactory);
            restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
                @Override
                // Ignore 400
                public void handleError(ClientHttpResponse response) throws IOException {
                    if (response.getRawStatusCode() != 400) {
                        super.handleError(response);
                    }
                }
            });
            remoteTokenServices.setRestTemplate(restTemplate);
        });
    }

    @Bean
    public RestTemplateCustomizer sslRestTemplateCustomizer(Optional<SSLContext> sslContext, HostnameVerifier verifier) {
        return sslContext.map(context -> {
            var requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom()
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(context, verifier))
                    .build());
            return (RestTemplateCustomizer) restTemplate -> restTemplate.setRequestFactory(requestFactory);
        }).orElse(restTemplate -> {
        });
    }

    @Bean
    @ConditionalOnProperty(name = "server.ssl.key-store")
    public static SSLContext sslContext(ServerProperties properties) throws Exception {
        var ssl = properties.getSsl();
        return new SSLContextBuilder()
                .loadTrustMaterial(null, new TrustSelfSignedStrategy())
                .loadKeyMaterial(
                        ResourceUtils.getFile(ssl.getKeyStore()),
                        ssl.getKeyStorePassword().toCharArray(),
                        ssl.getKeyPassword().toCharArray()
                ).build();
    }

    @Bean
    public static HostnameVerifier noopHostnameVerifier(SSLProperties properties) {
        if (properties.isCheck()) {
            return new DefaultHostnameVerifier(PublicSuffixMatcherLoader.getDefault());
        }
        return NoopHostnameVerifier.INSTANCE;
    }
}
