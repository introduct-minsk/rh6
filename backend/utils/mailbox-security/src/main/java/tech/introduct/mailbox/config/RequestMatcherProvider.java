package tech.introduct.mailbox.config;

import lombok.experimental.UtilityClass;
import org.springframework.security.web.util.matcher.NegatedRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import static org.apache.commons.lang3.StringUtils.startsWith;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import static org.springframework.security.oauth2.common.OAuth2AccessToken.BEARER_TYPE;

@UtilityClass
public class RequestMatcherProvider {

    static RequestMatcher hasBearerMatcher() {
        return request -> startsWith(request.getHeader(AUTHORIZATION), BEARER_TYPE);
    }

    static RequestMatcher hasNotBearerMatcher() {
        return new NegatedRequestMatcher(hasBearerMatcher());
    }
}
