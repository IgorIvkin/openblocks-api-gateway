package ru.openblocks.authmanagerservice.persistence.repository;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.map.LRUMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import ru.openblocks.authmanagerservice.persistence.entity.AccessTokenEntity;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@ConditionalOnProperty(name = "app.access-token-store.type", havingValue = "BASIC")
public class BasicAccessTokenRepository implements AccessTokenRepository {

    private final Map<UUID, AccessTokenEntity> storage = Collections.synchronizedMap(new LRUMap<>(1000));

    @Override
    public void store(UUID uuid, String clientId, Instant expiresIn, String jwtToken) {
        Objects.requireNonNull(uuid, "UUID should be not null to store access token");
        Objects.requireNonNull(clientId, "Client ID should be not null to store access token");

        log.info("Store access token with uuid {} by client ID {}", uuid, clientId);

        AccessTokenEntity accessToken = AccessTokenEntity.builder()
                .clientId(clientId)
                .jwtToken(jwtToken)
                .expiresIn(expiresIn)
                .build();
        storage.put(uuid, accessToken);
    }

    @Override
    public Optional<AccessTokenEntity> get(UUID uuid) {
        Objects.requireNonNull(uuid, "UUID should be not null to get access token");

        return Optional.ofNullable(storage.get(uuid));
    }
}
