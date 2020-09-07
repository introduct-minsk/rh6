package tech.introduct.mailbox.hadoop;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "mailbox.hadoop")
public class HadoopProperties {
    private String uri;
    private String user;
    private String path;
    private String homeDir = "/";

}
