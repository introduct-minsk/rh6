package tech.introduct.mailbox.persistence.domain;

import lombok.*;
import org.hibernate.annotations.NaturalId;

import javax.persistence.*;

@Entity
@Table(name = "mailbox_client_role")
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
public class RoleEntity {
    @Id
    @Access(AccessType.PROPERTY)
    @Column(name = "id", nullable = false, updatable = false)
    private Integer id;

    @NaturalId
    @Column(name = "role", nullable = false, updatable = false)
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id")
    private ClientDetailsEntity client;
}
