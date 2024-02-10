package ru.openblocks.authmanagerservice.api.dto.checkauth.get;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckAuthenticationRequest {

    @NotBlank
    private String login;

    @NotBlank
    private String password;
}