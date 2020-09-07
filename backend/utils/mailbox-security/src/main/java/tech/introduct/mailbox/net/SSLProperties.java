package tech.introduct.mailbox.net;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "server.ssl")
public class SSLProperties {
    private boolean check;

}
