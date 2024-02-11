package ru.openblocks.authmanagerservice.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenEntity {

    private String clientId;

    private Instant expiresIn;

    private String jwtToken;
}
