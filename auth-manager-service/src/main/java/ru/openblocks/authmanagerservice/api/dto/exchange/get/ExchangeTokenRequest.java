package ru.openblocks.authmanagerservice.api.dto.exchange.get;

import jakarta.validation.constraints.NotBlank;

public record ExchangeTokenRequest(
        @NotBlank String accessToken
) {
}
