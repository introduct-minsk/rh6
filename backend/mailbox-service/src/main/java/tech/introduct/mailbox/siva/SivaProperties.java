package tech.introduct.mailbox.siva;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "siva")
public class SivaProperties {
    private String serviceHost = "http://localhost:8080";
    private String validatePath = "/validate";

}
