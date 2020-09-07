package tech.introduct.mailbox.elasticsearch;

import org.apache.http.HttpHost;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientBuilderCustomizer;
import org.springframework.boot.autoconfigure.elasticsearch.rest.RestClientProperties;
import org.springframework.util.ReflectionUtils;

import javax.net.ssl.SSLContext;
import java.util.Optional;

class ElasticsearchConfigTest {

    @Test
    void elasticsearchCustomizer() throws Exception {
        var config = new ElasticsearchConfig();
        var context = Optional.of(SSLContext.getDefault());
        var verifier = NoopHostnameVerifier.INSTANCE;
        assertCustomizer(config.elasticsearchCustomizer(new RestClientProperties(), context, verifier));
        var properties = new RestClientProperties();
        properties.setUsername("username");
        properties.setPassword("password");
        assertCustomizer(config.elasticsearchCustomizer(properties, context, verifier));
    }

    private void assertCustomizer(RestClientBuilderCustomizer customizer) throws IllegalAccessException {
        var builder = RestClient.builder(new HttpHost("localhost"));
        customizer.customize(builder);
        var field = ReflectionUtils.findField(RestClientBuilder.class, "httpClientConfigCallback");
        if (field != null) {
            field.setAccessible(true);
            var callback = (RestClientBuilder.HttpClientConfigCallback) field.get(builder);
            callback.customizeHttpClient(HttpAsyncClientBuilder.create());
        }
    }
}