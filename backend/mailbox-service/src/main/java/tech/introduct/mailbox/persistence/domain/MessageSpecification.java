package tech.introduct.mailbox.persistence.domain;

import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import tech.introduct.mailbox.dto.MessageDirection;
import tech.introduct.mailbox.dto.MessageType;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@RequiredArgsConstructor
public class MessageSpecification implements Specification<MessageEntity> {
    private final String roleId;
    private final MessageDirection direction;

    @Override
    @Nullable
    public Predicate toPredicate(@NonNull Root<MessageEntity> root, @NonNull CriteriaQuery<?> query,
                                 @NonNull CriteriaBuilder criteriaBuilder) {
        query.orderBy(criteriaBuilder.desc(root.get(MessageEntity_.createdOn)));
        return directionPredicate(root, criteriaBuilder);
    }

    private Predicate directionPredicate(@NonNull Root<MessageEntity> root, @NonNull CriteriaBuilder criteriaBuilder) {
        if (direction == MessageDirection.IN) {
            return predicateIn(root, criteriaBuilder);
        } else if (direction == MessageDirection.OUT) {
            return predicateOut(root, criteriaBuilder);
        } else {
            return criteriaBuilder.or(
                    predicateIn(root, criteriaBuilder),
                    predicateOut(root, criteriaBuilder)
            );
        }
    }

    private Predicate predicateOut(@NonNull Root<MessageEntity> root, @NonNull CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.and(
                criteriaBuilder.equal(root.get(MessageEntity_.sender), roleId),
                criteriaBuilder.notEqual(root.get(MessageEntity_.type), MessageType.NOTIFICATION)
        );
    }

    private Predicate predicateIn(@NonNull Root<MessageEntity> root, @NonNull CriteriaBuilder criteriaBuilder) {
        return criteriaBuilder.equal(root.get(MessageEntity_.receiver), roleId);
    }
}
