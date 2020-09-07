package tech.introduct.mailbox.client;

import feign.Client;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.cloud.security.oauth2.client.feign.OAuth2FeignRequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.AccessTokenProviderChain;
import org.springframework.security.oauth2.client.token.grant.client.ClientCredentialsAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.implicit.ImplicitAccessTokenProvider;
import org.springframework.security.oauth2.client.token.grant.password.ResourceOwnerPasswordAccessTokenProvider;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.util.List;
import java.util.Optional;

@Configuration
@EnableFeignClients
@EnableConfigurationProperties(FeignClientsProperties.class)
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class FeignClientsConfig {

    @Bean
    @SuppressWarnings("deprecation")
    public OAuth2FeignRequestInterceptor feignInterceptor(OAuth2ClientContext oAuth2ClientContext,
                                                          OAuth2ProtectedResourceDetails resource,
                                                          Optional<SSLContext> sslContext, HostnameVerifier verifier) {
        var interceptor = new OAuth2FeignRequestInterceptor(oAuth2ClientContext, resource);

        sslContext.ifPresent(context -> {
            var requestFactory = new HttpComponentsClientHttpRequestFactory(HttpClients.custom()
                    .setSSLSocketFactory(new SSLConnectionSocketFactory(context, verifier))
                    .build());

            var providers = List.of(
                    new AuthorizationCodeAccessTokenProvider(),
                    new ImplicitAccessTokenProvider(),
                    new ResourceOwnerPasswordAccessTokenProvider(),
                    new ClientCredentialsAccessTokenProvider()
            );
            providers.forEach(provider -> provider.setRequestFactory(requestFactory));

            interceptor.setAccessTokenProvider(new AccessTokenProviderChain(providers));
        });

        return interceptor;
    }

    @Bean
    public Client sslFeignClient(Optional<SSLContext> sslContext, HostnameVerifier verifier) {
        return sslContext
                .map(context -> new Client.Default(context.getSocketFactory(), verifier))
                .orElseGet(() -> new Client.Default(null, null));
    }
}
