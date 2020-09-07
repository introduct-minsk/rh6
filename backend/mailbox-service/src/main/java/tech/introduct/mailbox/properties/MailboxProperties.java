package tech.introduct.mailbox.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mailbox")
public class MailboxProperties {
    private int maxAttachmentNumber = 25;

}
