package ru.openblocks.authmanagerservice.persistence.repository;

import org.springframework.stereotype.Repository;
import ru.openblocks.authmanagerservice.persistence.entity.AccessTokenEntity;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessTokenRepository {

    void store(UUID uuid, String clientId, Instant expiresIn, String jwtToken);

    Optional<AccessTokenEntity> get(UUID uuid);
}
