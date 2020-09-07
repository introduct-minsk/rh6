package tech.introduct.mailbox.xroad;

import tech.introduct.mailbox.dto.UserDto;
import tech.introduct.mailbox.dto.user.UserRole;

import java.util.Set;

public interface XRoadClient {

    UserDto findUser(String identityCodes);

    Set<UserRole> findRoles(String identityCodes);
}
