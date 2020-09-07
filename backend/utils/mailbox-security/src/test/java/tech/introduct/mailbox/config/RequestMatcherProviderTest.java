package tech.introduct.mailbox.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static tech.introduct.mailbox.config.RequestMatcherProvider.hasBearerMatcher;
import static tech.introduct.mailbox.config.RequestMatcherProvider.hasNotBearerMatcher;

class RequestMatcherProviderTest {

    @Test
    void testHasBearerMatcher() {
        var request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION, "Bearer token");
        assertTrue(hasBearerMatcher().matcher(request).isMatch());

        request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION, "Basic token");
        assertFalse(hasBearerMatcher().matcher(request).isMatch());
    }

    @Test
    void testHasNotBearerMatcher() {
        var request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION, "Bearer token");
        assertFalse(hasNotBearerMatcher().matcher(request).isMatch());

        request = new MockHttpServletRequest();
        request.addHeader(AUTHORIZATION, "Basic token");
        assertTrue(hasNotBearerMatcher().matcher(request).isMatch());
    }
}
