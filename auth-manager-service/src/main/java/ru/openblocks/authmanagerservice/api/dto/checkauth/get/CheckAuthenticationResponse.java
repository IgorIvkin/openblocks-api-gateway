package ru.openblocks.authmanagerservice.api.dto.checkauth.get;

public record CheckAuthenticationResponse(
        Boolean status,

        Long id
) {
}

