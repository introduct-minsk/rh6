package tech.introduct.mailbox.web.controller;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.*;
import tech.introduct.mailbox.dto.SettingsDto;
import tech.introduct.mailbox.dto.UserDto;
import tech.introduct.mailbox.dto.user.UserRole;
import tech.introduct.mailbox.service.UserService;

import java.util.List;

@RestController
@RequestMapping(path = "/api/v1/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Slf4j
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    @JsonView(UserDto.View.Detailed.class)
    public UserDto getMe(@AuthenticationPrincipal OAuth2User user) {
        log.debug("getMe() for {}", user);
        return userService.findById(user.getName());
    }

    @GetMapping("/me/roles")
    public List<UserRole> getRoles(@AuthenticationPrincipal OAuth2User user) {
        return userService.findRoles(user.getName());
    }

    @PostMapping("/me/role")
    public UserRole setCurrentRole(@AuthenticationPrincipal OAuth2User user, @RequestBody UserRole role) {
        return userService.setCurrentRole(user.getName(), role);
    }

    @GetMapping("/me/settings")
    public SettingsDto getSettings(@AuthenticationPrincipal OAuth2User user) {
        return userService.findSettings(user.getName());
    }

    @PostMapping("/me/settings")
    public SettingsDto updateSettings(@AuthenticationPrincipal OAuth2User user, @RequestBody SettingsDto settingsDto) {
        return userService.updateSettings(user.getName(), settingsDto);
    }
}
