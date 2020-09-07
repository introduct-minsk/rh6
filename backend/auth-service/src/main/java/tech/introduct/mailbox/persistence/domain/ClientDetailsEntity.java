package tech.introduct.mailbox.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "mailbox_client")
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public class ClientDetailsEntity {
    @Id
    @Access(AccessType.PROPERTY)
    @Column(name = "client_id", nullable = false, updatable = false)
    private String clientId;

    @Column(name = "secret", nullable = false)
    @JsonIgnore
    private String secret;

    @OneToMany(mappedBy = "client")
    private List<RoleEntity> roles = List.of();
}
