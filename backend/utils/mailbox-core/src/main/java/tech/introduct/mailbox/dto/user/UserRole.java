package tech.introduct.mailbox.dto.user;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public class UserRole implements Serializable {
    private String id;

}
