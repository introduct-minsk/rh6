package tech.introduct.mailbox.client;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "feign.client")
public class FeignClientsProperties {
    private Service searchService;
    private Service userService;

    @Data
    @NoArgsConstructor
    public static class Service {
        private String url;
    }
}
