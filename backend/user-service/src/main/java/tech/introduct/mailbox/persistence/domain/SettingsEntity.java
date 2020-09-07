package tech.introduct.mailbox.persistence.domain;

import lombok.*;

import javax.persistence.*;
import java.util.Locale;

@Entity
@Table(name = "mailbox_user_settings")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@RequiredArgsConstructor
@Setter(AccessLevel.PROTECTED)
public class SettingsEntity {
    @Id
    @Access(AccessType.PROPERTY)
    @Column(name = "user_id", nullable = false, updatable = false)
    @NonNull
    private String userId;

    @Column(name = "locale", nullable = false)
    @Setter
    @NonNull
    private Locale locale;

}
