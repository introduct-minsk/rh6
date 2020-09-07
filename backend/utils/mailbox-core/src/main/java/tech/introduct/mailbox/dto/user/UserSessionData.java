package tech.introduct.mailbox.dto.user;

import lombok.Data;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import tech.introduct.mailbox.web.handler.ErrorInfo;

import java.io.Serializable;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
@SessionScope
@Data
public class UserSessionData implements Serializable {
    private UserRole currentRole;

    public String requiredCurrentRoleId() {
        if (currentRole == null || isEmpty(currentRole.getId())) {
            throw new ErrorInfo("role_not_set").badRequest();
        }
        return currentRole.getId();
    }
}
