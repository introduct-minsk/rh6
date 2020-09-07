package tech.introduct.mailbox.xroad;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.util.Optional;

import static java.util.Optional.ofNullable;
import static org.apache.commons.lang3.StringUtils.trimToNull;

@Data
@ConfigurationProperties(prefix = "xroad")
@Validated
public class XRoadProperties {
    /**
     * X-Road security server
     */
    @NotNull
    private String securityServer;
    /**
     * X-Road id code
     */
    @NotNull
    private String idCode;
    private String file;
    @NotNull
    @Valid
    private Client client;
    @NotNull
    @Valid
    private Service rr;
    @NotNull
    @Valid
    private Service aar;
    private Duration connectionTimeout = Duration.ofSeconds(5);
    private Duration readTimeout = Duration.ofSeconds(5);

    public Service getService(String database) {
        if ("rr".equals(database)) {
            return rr;
        }
        if ("aar".equals(database)) {
            return aar;
        }
        throw new IllegalArgumentException("not found xRoad database config for " + database);
    }

    @Data
    public static class Client {
        /**
         * code that identifies the X-Road instance
         */
        @NotNull
        private String instance;
        /**
         * code that identifies the member class
         */
        @NotNull
        private String memberClass;
        /**
         * code that identifies the X-Road member
         */
        @NotNull
        private String memberCode;
        /**
         * (optional) code that identifies a subsystem of the given member;
         */
        private String subsystemCode;
        private String objectType;

        public Optional<String> getObjectType() {
            return ofNullable(trimToNull(objectType));
        }
    }

    @Data
    @ToString(callSuper = true)
    @EqualsAndHashCode(callSuper = true)
    public static class Service extends Client {
        /**
         * version of the service
         */
        @NotNull
        private String protocolVersion;
    }
}
