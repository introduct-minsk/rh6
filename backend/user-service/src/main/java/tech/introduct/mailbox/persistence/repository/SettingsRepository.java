package tech.introduct.mailbox.persistence.repository;

import org.springframework.data.repository.Repository;
import tech.introduct.mailbox.persistence.domain.SettingsEntity;

import java.util.Optional;

public interface SettingsRepository extends Repository<SettingsEntity, String> {

    Optional<SettingsEntity> findByUserId(String userId);

    <S extends SettingsEntity> S save(S entity);
}
