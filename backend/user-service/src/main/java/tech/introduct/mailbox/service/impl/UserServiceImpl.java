package tech.introduct.mailbox.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.introduct.mailbox.dto.SettingsDto;
import tech.introduct.mailbox.dto.UserDto;
import tech.introduct.mailbox.dto.user.UserRole;
import tech.introduct.mailbox.dto.user.UserSessionData;
import tech.introduct.mailbox.persistence.domain.SettingsEntity;
import tech.introduct.mailbox.persistence.repository.SettingsRepository;
import tech.introduct.mailbox.service.UserService;
import tech.introduct.mailbox.web.handler.ErrorInfo;
import tech.introduct.mailbox.web.handler.ErrorInfoRuntimeException;
import tech.introduct.mailbox.xroad.XRoadClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final SettingsRepository settingsRepository;
    private final XRoadClient xRoadClient;
    private final UserSessionData sessionData;

    @Override
    public UserDto findById(String userId) {
        if (sessionData.getCurrentRole() == null) {
            try {
                var roles = findRoles(userId);
                if (roles.size() == 1) {
                    sessionData.setCurrentRole(roles.get(0));
                }
            } catch (Exception e) {
                log.warn("when try set role", e);
            }
        }
        return xRoadClient.findUser(userId).toBuilder()
                .role(sessionData.getCurrentRole())
                .build();
    }

    @Override
    public List<UserRole> findRoles(String userId) {
        var userRoles = xRoadClient.findRoles(userId);
        var result = new ArrayList<UserRole>(userRoles.size() + 1);
        result.add(new UserRole(userId));
        result.addAll(userRoles);
        return result;
    }

    @Override
    public UserRole setCurrentRole(String userId, UserRole role) {
        try {
            if (!findRoles(userId).contains(role)) {
                throw new ErrorInfo("not_allowed").badRequest();
            }
        } catch (Exception e) {
            log.warn("when try set role", e);
            if (!userId.equals(role.getId())) {
                throw new ErrorInfo("not_allowed").badRequest();
            }
        }
        sessionData.setCurrentRole(role);
        return role;
    }

    @Override
    public List<UserDto> findByIds(Set<String> userIds) {
        return userIds.stream()
                .map(identityCodes -> {
                    try {
                        return xRoadClient.findUser(identityCodes);
                    } catch (ErrorInfoRuntimeException e) {
                        return UserDto.builder()
                                .id(identityCodes)
                                .build();
                    }
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public SettingsDto findSettings(String userId) {
        return settingsRepository.findByUserId(userId)
                .map(settings -> new SettingsDto(settings.getLocale()))
                .orElseGet(() -> new SettingsDto(new Locale("ee")));
    }

    @Override
    @Transactional
    public SettingsDto updateSettings(String userId, SettingsDto settingsDto) {
        var settings = settingsRepository.findByUserId(userId)
                .map(entity -> {
                    entity.setLocale(settingsDto.getLocale());
                    return entity;
                }).orElseGet(() -> new SettingsEntity(userId, settingsDto.getLocale()));
        settingsRepository.save(settings);
        return new SettingsDto(settings.getLocale());
    }
}
