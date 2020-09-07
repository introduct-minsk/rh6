package tech.introduct.mailbox.elasticsearch;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientBuilderCustomizer;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import java.util.Optional;

@Configuration
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
public class ElasticsearchConfig {

    @Bean
    RestClientBuilderCustomizer elasticsearchCustomizer(RestClientProperties properties,
                                                        Optional<SSLContext> sslContext, HostnameVerifier verifier) {
        return builder -> sslContext.ifPresent(context -> builder.setHttpClientConfigCallback(httpBuilder -> {
            if (StringUtils.isNotEmpty(properties.getUsername())) {
                CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                Credentials credentials = new UsernamePasswordCredentials(properties.getUsername(), properties.getPassword());
                credentialsProvider.setCredentials(AuthScope.ANY, credentials);
                httpBuilder.setDefaultCredentialsProvider(credentialsProvider);
            }
            return httpBuilder
                    .setSSLContext(context)
                    .setSSLHostnameVerifier(verifier);
        }));
    }
}
