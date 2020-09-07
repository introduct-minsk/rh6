package tech.introduct.mailbox.service;

import tech.introduct.mailbox.dto.SettingsDto;
import tech.introduct.mailbox.dto.UserDto;
import tech.introduct.mailbox.dto.user.UserRole;

import java.util.List;
import java.util.Set;

public interface UserService {

    UserDto findById(String userId);

    List<UserRole> findRoles(String userId);

    UserRole setCurrentRole(String userId, UserRole role);

    List<UserDto> findByIds(Set<String> userIds);

    SettingsDto findSettings(String userId);

    SettingsDto updateSettings(String userId, SettingsDto settingsDto);
}
