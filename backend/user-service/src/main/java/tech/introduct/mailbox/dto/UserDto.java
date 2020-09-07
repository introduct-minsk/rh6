package tech.introduct.mailbox.dto;

import com.fasterxml.jackson.annotation.JsonView;
import lombok.Builder;
import lombok.Value;
import tech.introduct.mailbox.dto.user.UserRole;

import java.io.Serializable;

@Value
@Builder(toBuilder = true)
@JsonView(UserDto.View.Detailed.class)
public class UserDto implements Serializable {
    @JsonView(UserDto.View.General.class)
    String id;
    @JsonView(UserDto.View.General.class)
    String firstName;
    @JsonView(UserDto.View.General.class)
    String lastName;
    String dateOfBirth;
    String address;
    UserRole role;

    public interface View {
        interface General {
        }

        interface Detailed extends General {
        }
    }
}
