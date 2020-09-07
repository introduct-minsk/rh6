package tech.introduct.mailbox.client;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tech.introduct.mailbox.dto.UserDto;
import tech.introduct.mailbox.web.handler.ErrorInfo;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Profile("!test")
@FeignClient(url = "${feign.client.user-service.url}", name = "user-service")
public interface UserClient {

    default Map<String, UserDto> getUserDetailsMap(Collection<String> ids) {
        try {
            return apiGetUserDetails(new HashSet<>(ids)).stream()
                    .collect(Collectors.toUnmodifiableMap(UserDto::getId, Function.identity()));
        } catch (Exception e) {
            LogHolder.log.error("get user details", e);
            throw new ErrorInfo("user_service_unavailable").unavailable();
        }
    }

    @GetMapping(path = "/private/api/users", consumes = MediaType.APPLICATION_JSON_VALUE)
    Set<UserDto> apiGetUserDetails(@RequestParam Set<String> id);

    @Slf4j
    @UtilityClass
    final class LogHolder {
    }
}
