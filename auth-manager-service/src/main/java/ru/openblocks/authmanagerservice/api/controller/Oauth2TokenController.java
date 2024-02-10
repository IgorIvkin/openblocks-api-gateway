package ru.openblocks.authmanagerservice.api.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.openblocks.authmanagerservice.api.dto.token.get.Oauth2TokenRequest;
import ru.openblocks.authmanagerservice.api.dto.token.get.Oauth2TokenResponse;
import ru.openblocks.authmanagerservice.service.AuthenticationService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/oauth2")
public class Oauth2TokenController {

    private final AuthenticationService authenticationService;

    @PostMapping(value = "/token",
            produces = {MediaType.APPLICATION_JSON_VALUE},
            consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE})
    public Mono<Oauth2TokenResponse> token(ServerWebExchange serverWebExchange) {
        return serverWebExchange.getFormData()
                .flatMap(formData -> authenticationService.getToken(getOauth2TokenRequest(formData)));
    }

    @GetMapping(value = "/.well-known/jwks.json",
            produces = {MediaType.APPLICATION_JSON_VALUE})
    public Mono<String> getJwks() {
        return authenticationService.getJwks();
    }

    private Oauth2TokenRequest getOauth2TokenRequest(MultiValueMap<String, String> formData) {

        // check that client ID is defined
        List<String> clientIds = formData.get("client_id");
        if (CollectionUtils.isEmpty(clientIds)) {
            throw new IllegalArgumentException("client_id should be specified");
        }
        final String clientId = clientIds.getFirst();

        // check that client secret is defined
        List<String> clientSecrets = formData.get("client_secret");
        if (CollectionUtils.isEmpty(clientSecrets)) {
            throw new IllegalArgumentException("client_secret should be specified");
        }
        final String clientSecret = clientSecrets.getFirst();

        return Oauth2TokenRequest.builder()
                .clientId(clientId)
                .clientSecret(clientSecret)
                .build();
    }
}
