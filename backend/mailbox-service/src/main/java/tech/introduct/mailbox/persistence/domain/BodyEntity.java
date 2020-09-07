package tech.introduct.mailbox.persistence.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "message_body")
@Data
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "message")
@ToString(of = "text")
public class BodyEntity {
    @Id
    @Access(AccessType.PROPERTY)
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "text", updatable = false)
    private String text;

    @OneToOne(mappedBy = "body", fetch = FetchType.LAZY)
    @JsonIgnore
    private MessageEntity message;

    BodyEntity(MessageEntity message, String text) {
        this.text = text;
        this.message = message;
    }
}
