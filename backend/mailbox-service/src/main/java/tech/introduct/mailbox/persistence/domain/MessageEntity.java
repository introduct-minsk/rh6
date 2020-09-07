package tech.introduct.mailbox.persistence.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.Hibernate;
import tech.introduct.mailbox.dto.MessageType;
import tech.introduct.mailbox.dto.NotificationType;

import javax.persistence.*;
import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static tech.introduct.mailbox.dto.MessageType.NOTIFICATION;

@Entity
@Table(name = "message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Setter(AccessLevel.PROTECTED)
@EqualsAndHashCode(of = "createdOn")
@ToString(exclude = "body")
public class MessageEntity {
    @Id
    @Access(AccessType.PROPERTY)
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType type;

    @Column(name = "subject", updatable = false)
    private String subject;

    @Column(name = "created_on", nullable = false, updatable = false)
    private ZonedDateTime createdOn = ZonedDateTime.now();

    @Column(name = "unread", nullable = false)
    @Setter
    private boolean unread = true;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_message_id")
    private MessageEntity related;

    @Column(name = "sender_user_id", nullable = false, updatable = false)
    private String senderUserId;

    @Column(name = "sender", nullable = false, updatable = false)
    private String sender;

    @Column(name = "receiver", nullable = false, updatable = false)
    private String receiver;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "body_id")
    private BodyEntity body;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sign_id")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private FileEntity sign;

    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "attachment_for")
    private Set<FileEntity> attachments = Set.of();

    @Builder
    protected MessageEntity(MessageType type, String senderUserId, String sender,
                            String receiver, String subject, String text, MessageEntity related,
                            FileEntity sign, @Singular Set<FileEntity> attachments) {
        this.type = requireNonNull(type);
        if (isNotBlank(subject)) {
            this.subject = subject.trim();
        }
        this.related = related;
        this.senderUserId = requireNonNull(senderUserId);
        this.sender = requireNonNull(sender);
        this.receiver = requireNonNull(receiver);
        if (isNotBlank(text)) {
            this.body = new BodyEntity(this, text.trim());
        }
        this.sign = sign;
        this.attachments = attachments;
    }

    public static MessageEntity toNotification(NotificationType type, MessageEntity related, String senderUserId) {
        return builder()
                .senderUserId(requireNonNull(senderUserId))
                .sender(related.receiver)
                .receiver(related.sender)
                .type(NOTIFICATION)
                .subject(type.name())
                .related(related)
                .build();
    }

    protected void setBody(BodyEntity body) {
        this.body = body;
        if (this.body != null) {
            this.body.setMessage(this);
        }
    }

    public MessageEntity unproxy() {
        if (related != null) {
            related = Hibernate.unproxy(related.unproxy(), MessageEntity.class);
        }
        body = Hibernate.unproxy(body, BodyEntity.class);
        sign = Hibernate.unproxy(sign, FileEntity.class);
        attachments = attachments.stream()
                .map(file -> Hibernate.unproxy(file, FileEntity.class))
                .collect(Collectors.toUnmodifiableSet());
        return this;
    }
}
