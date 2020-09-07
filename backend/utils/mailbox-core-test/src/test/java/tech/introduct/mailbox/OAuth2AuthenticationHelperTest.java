package tech.introduct.mailbox;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;

import static org.junit.jupiter.api.Assertions.assertThrows;

class OAuth2AuthenticationHelperTest {
    private final OAuth2AuthenticationHelper helper = new OAuth2AuthenticationHelper();
    private ResourceServerTokenServices tokenServices = helper.resourceServerTokenServices();

    @Test
    void loadAuthenticationWithoutSetup() {
        assertThrows(InvalidTokenException.class,
                () -> tokenServices.loadAuthentication("not-valid"));
    }

    @Test
    void readAccessTokenUnsupported() {
        assertThrows(UnsupportedOperationException.class,
                () -> tokenServices.readAccessToken("not-valid"));
    }
}
