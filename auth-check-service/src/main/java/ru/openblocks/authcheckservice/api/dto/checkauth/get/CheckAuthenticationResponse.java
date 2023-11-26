package ru.openblocks.authcheckservice.api.dto.checkauth.get;

public record CheckAuthenticationResponse(
        Boolean status,

        Long id
) {
}
