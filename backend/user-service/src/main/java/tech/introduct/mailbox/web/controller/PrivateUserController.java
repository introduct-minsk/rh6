package tech.introduct.mailbox.web.controller;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.introduct.mailbox.dto.UserDto;
import tech.introduct.mailbox.service.UserService;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping(path = "/private/api/users", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class PrivateUserController {
    private final UserService userService;

    @GetMapping
    @Secured("ROLE_GET_USER_DETAILS")
    @JsonView(UserDto.View.General.class)
    public List<UserDto> getUsers(@RequestParam Set<String> id) {
        return userService.findByIds(id);
    }
}
