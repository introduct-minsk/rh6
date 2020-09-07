package tech.introduct.mailbox.persistence.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.Repository;
import tech.introduct.mailbox.persistence.domain.MessageEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

public interface MessageRepository extends Repository<MessageEntity, UUID> {

    Optional<MessageEntity> findById(UUID id);

    Page<MessageEntity> findAll(Specification<MessageEntity> spec, Pageable pageable);

    Stream<MessageEntity> findAllById(Iterable<UUID> ids);

    List<MessageEntity> findBySenderAndReceiver(String sender, String receiver);

    Page<MessageEntity> findAll(Pageable pageable);

    <S extends MessageEntity> S save(S entity);
}
