package tech.introduct.mailbox.xroad;

import com.nortal.jroad.client.enums.XroadObjectType;
import com.nortal.jroad.client.service.configuration.SimpleXRoadServiceConfiguration;
import com.nortal.jroad.client.service.configuration.XRoadServiceConfiguration;
import com.nortal.jroad.client.service.configuration.provider.AbstractXRoadServiceConfigurationProvider;
import com.nortal.jroad.client.service.configuration.provider.XRoadServiceConfigurationProvider;
import com.nortal.jroad.client.service.consumer.StandardXRoadConsumer;
import com.nortal.jroad.client.util.WSConsumptionLoggingInterceptor;
import com.nortal.jroad.enums.XRoadProtocolVersion;
import ee.riik.xtee.client.database.AarXRoadDatabase;
import ee.riik.xtee.client.database.AarXRoadDatabaseImpl;
import ee.riik.xtee.client.database.RrXRoadDatabase;
import ee.riik.xtee.client.database.RrXRoadDatabaseImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ws.client.support.interceptor.ClientInterceptor;
import org.springframework.ws.transport.WebServiceMessageSender;
import org.springframework.ws.transport.http.HttpUrlConnectionMessageSender;
import tech.introduct.mailbox.xroad.impl.LoggingClientInterceptor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

@Configuration
@EnableConfigurationProperties(XRoadProperties.class)
@RequiredArgsConstructor
@Slf4j
public class XRoadConfig {

    /**
     * The configuration that use resource files xroad.properties and xroad-%s.properties files.
     *
     * @return X-Road configuration provider
     */
    @Bean
    public XRoadServiceConfigurationProvider configurationProvider(XRoadProperties properties) {
        return new AbstractXRoadServiceConfigurationProvider() {
            @Override
            protected XRoadServiceConfiguration fillConfuguration(SimpleXRoadServiceConfiguration config) {
                config.setSecurityServer(properties.getSecurityServer());
                config.setIdCode(properties.getIdCode());
                config.setFile(properties.getFile());

                var client = properties.getClient();
                config.setClientXRoadInstance(client.getInstance());
                config.setClientMemberClass(client.getMemberClass());
                config.setClientMemberCode(client.getMemberCode());
                config.setClientSubsystemCode(client.getSubsystemCode());
                client.getObjectType().map(XroadObjectType::valueOf).ifPresent(config::setClientObjectType);

                var service = properties.getService(config.getDatabase());
                config.setProtocolVersion(XRoadProtocolVersion.getValueByVersionCode(service.getProtocolVersion()));
                config.setServiceXRoadInstance(service.getInstance());
                config.setServiceMemberClass(service.getMemberClass());
                config.setServiceMemberCode(service.getMemberCode());
                config.setServiceSubsystemCode(service.getSubsystemCode());
                service.getObjectType().map(XroadObjectType::valueOf).ifPresent(config::setServiceObjectType);

                return config;
            }
        };
    }

    /**
     * @return X-Road RR Database
     */
    @Bean
    public RrXRoadDatabase rrXRoadDatabase() {
        return new RrXRoadDatabaseImpl();
    }

    /**
     * @return X-Road AAR Database
     */
    @Bean
    public AarXRoadDatabase aarXRoadDatabase() {
        return new AarXRoadDatabaseImpl();
    }

    /**
     * Service consumer X-Road data exchange processes with additional logger
     */
    @Bean
    public StandardXRoadConsumer standardXRoadConsumer(XRoadProperties properties) {
        StandardXRoadConsumer xRoadConsumer = new StandardXRoadConsumer() {
            @Override
            protected Collection<ClientInterceptor> createInterceptors() {
                var interceptors = new LinkedList<>(super.createInterceptors());
                interceptors.removeIf(interceptor -> interceptor instanceof WSConsumptionLoggingInterceptor);
                interceptors.add(new LoggingClientInterceptor("XRoad"));
                return Collections.unmodifiableList(interceptors);
            }
        };
        for (WebServiceMessageSender messageSender : xRoadConsumer.getMessageSenders()) {
            setTimeouts(messageSender, properties);
        }
        return xRoadConsumer;
    }

    void setTimeouts(WebServiceMessageSender messageSender, XRoadProperties properties) {
        if (messageSender instanceof HttpUrlConnectionMessageSender) {
            ((HttpUrlConnectionMessageSender) messageSender).setConnectionTimeout(properties.getConnectionTimeout());
            ((HttpUrlConnectionMessageSender) messageSender).setReadTimeout(properties.getReadTimeout());
        }
    }
}
