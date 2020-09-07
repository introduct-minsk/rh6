package tech.introduct.mailbox.utils;

import lombok.experimental.UtilityClass;
import tech.introduct.mailbox.client.SearchClient;
import tech.introduct.mailbox.client.UserClient;
import tech.introduct.mailbox.dto.UserDto;
import tech.introduct.mailbox.dto.user.UserSessionData;

import java.util.Collection;

import static java.util.stream.Collectors.toSet;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

@UtilityClass
public class MockUtils {

    public static void configureMock(UserClient userClient) {
        when(userClient.getUserDetailsMap(anyCollection())).thenCallRealMethod();
        when(userClient.apiGetUserDetails(anySet())).thenAnswer(invocation -> {
            Collection<String> argument = invocation.getArgument(0);
            return argument.stream().map(id -> new UserDto(id, "F" + id, "L" + id, null))
                    .collect(toSet());
        });
    }

    public static void configureMock(SearchClient searchClient) {
        when(searchClient.findSourceIds(any(), any(), any())).thenCallRealMethod();
        doCallRealMethod().when(searchClient).createSource(any());
    }

    public static void configureMock(UserSessionData userSessionData) {
        doCallRealMethod().when(userSessionData).setCurrentRole(any());
        when(userSessionData.getCurrentRole()).thenCallRealMethod();
        when(userSessionData.requiredCurrentRoleId()).thenCallRealMethod();
    }
}
