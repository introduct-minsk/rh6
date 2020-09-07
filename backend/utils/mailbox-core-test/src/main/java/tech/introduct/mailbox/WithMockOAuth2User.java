package tech.introduct.mailbox;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.test.context.support.WithSecurityContext;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Map;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.RandomStringUtils.randomNumeric;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockOAuth2User.SecurityContextFactory.class)
public @interface WithMockOAuth2User {
    String RANDOM = "random";

    String[] authorities() default {"SCOPE_openid", "ROLE_USER"};

    String clientRegistrationId() default "tara";

    String sub() default RANDOM;

    class SecurityContextFactory implements WithSecurityContextFactory<WithMockOAuth2User> {

        @Override
        public SecurityContext createSecurityContext(WithMockOAuth2User annotation) {
            var ctx = SecurityContextHolder.createEmptyContext();
            ctx.setAuthentication(getAuthentication(annotation));
            SecurityContextHolder.setContext(ctx);
            return ctx;
        }

        private Authentication getAuthentication(WithMockOAuth2User annotation) {
            var sub = annotation.sub().equals(RANDOM) ? randomNumeric(11) : annotation.sub();
            if (sub.length() == 11) {
                sub = "EE" + sub;
            }
            var authorities = stream(annotation.authorities()).map(SimpleGrantedAuthority::new).collect(toList());
            var oAuth2User = new DefaultOAuth2User(authorities, Map.of(
                    JwtClaimNames.SUB, sub
            ), JwtClaimNames.SUB);
            return new OAuth2AuthenticationToken(oAuth2User, authorities, annotation.clientRegistrationId());
        }
    }
}
