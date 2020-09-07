package tech.introduct.mailbox.persistence.repository;

import org.springframework.data.repository.Repository;
import tech.introduct.mailbox.persistence.domain.FileEntity;

import java.util.Optional;
import java.util.UUID;

public interface FileRepository extends Repository<FileEntity, UUID> {

    Optional<FileEntity> findById(UUID id);

    <S extends FileEntity> S save(S entity);
}
