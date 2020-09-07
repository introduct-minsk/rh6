package tech.introduct.mailbox.persistence.repository;

import org.springframework.data.repository.Repository;
import tech.introduct.mailbox.persistence.domain.ClientDetailsEntity;

import java.util.Optional;

public interface ClientDetailsRepository extends Repository<ClientDetailsEntity, String> {

    Optional<ClientDetailsEntity> findByClientId(String clientId);
}
