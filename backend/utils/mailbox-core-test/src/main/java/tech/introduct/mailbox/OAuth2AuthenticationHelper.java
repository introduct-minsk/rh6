package tech.introduct.mailbox;

import lombok.Value;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.springframework.security.oauth2.common.OAuth2AccessToken.BEARER_TYPE;

@Configuration
public class OAuth2AuthenticationHelper {
    private static Map<String, TokenInfo> tokens = new HashMap<>();

    public static String bearerAdmin() {
        return bearerWithRole("ROLE_DB_READ_WRITE");
    }

    public static String bearerWithRole(String role) {
        return bearer("service-client", role);
    }

    public static String bearer(String clientId, String role) {
        var token = RandomStringUtils.randomAlphanumeric(10, 20);
        tokens.put(token, new TokenInfo(clientId, List.of(role)));
        return BEARER_TYPE + " " + token;
    }

    @Bean
    @Primary
    public ResourceServerTokenServices resourceServerTokenServices() {
        return new ResourceServerTokenServices() {

            @Override
            public OAuth2Authentication loadAuthentication(String accessToken) {
                var tokenInfo = tokens.get(accessToken);
                if (tokenInfo == null) {
                    throw new InvalidTokenException(accessToken);
                }
                var authorities = tokenInfo.role.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toUnmodifiableSet());
                return new OAuth2Authentication(new OAuth2Request(
                        Map.of("client_id", tokenInfo.clientId),
                        tokenInfo.clientId,
                        authorities,
                        true,
                        Set.of("any"),
                        Set.of(),
                        null,
                        Set.of(),
                        Map.of()
                ), null);
            }

            @Override
            public OAuth2AccessToken readAccessToken(String accessToken) {
                throw new UnsupportedOperationException("Not supported: read access token");
            }
        };
    }

    @Value
    private static class TokenInfo {
        String clientId;
        List<String> role;
    }
}
