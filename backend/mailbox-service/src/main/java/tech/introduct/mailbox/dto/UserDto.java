package tech.introduct.mailbox.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Value;

@Value
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    String id;
    String firstName;
    String lastName;
    String roleId;

    public UserDto withRoleId(String roleId) {
        return new UserDto(id, firstName, lastName, roleId);
    }

    @JsonIgnore
    public boolean isOnBehalfOf() {
        return roleId != null && !roleId.equals(id);
    }
}
