package tech.introduct.mailbox.utils;

import lombok.experimental.UtilityClass;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.core.context.SecurityContext;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;

@UtilityClass
public class SessionUtils {

    public static String getUserId(MockHttpSession session) {
        var securityContext = ((SecurityContext) session.getAttribute(SPRING_SECURITY_CONTEXT_KEY));
        return securityContext.getAuthentication().getName();
    }
}
