package ru.openblocks.authmanagerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.openblocks.authmanagerservice.api.dto.checkauth.get.CheckAuthenticationRequest;
import ru.openblocks.authmanagerservice.api.dto.token.get.Oauth2TokenRequest;
import ru.openblocks.authmanagerservice.api.dto.token.get.Oauth2TokenResponse;
import ru.openblocks.authmanagerservice.exception.BadCredentialsException;

@Slf4j
@Service
public class AuthenticationService {

    private final TokenService tokenService;

    private final CheckAuthenticationService checkAuthenticationService;

    @Autowired
    public AuthenticationService(TokenService tokenService,
                                 CheckAuthenticationService checkAuthenticationService) {
        this.tokenService = tokenService;
        this.checkAuthenticationService = checkAuthenticationService;
    }

    /**
     * Validates user credentials and issues a token for user if everything is ok.
     * Throws 401 in case if credentials are invalid.
     *
     * @param request login and password of user
     * @return token in case credentials are ok
     */
    public Mono<Oauth2TokenResponse> getToken(Oauth2TokenRequest request) {

        final String clientId = request.getClientId();
        final String clientSecret = request.getClientSecret();

        log.info("Check credentials for client {}", clientId);

        final CheckAuthenticationRequest authRequest = CheckAuthenticationRequest.builder()
                .login(clientId)
                .password(clientSecret)
                .build();
        return checkAuthenticationService.checkAuthentication(authRequest)
                .flatMap(r -> checkAuthenticationResult(clientId, r));
    }

    /**
     * Returns current JWK for JWKs endpoint.
     *
     * @return current JWK as JWKS
     */
    public Mono<String> getJwks() {
        return Mono.fromSupplier(tokenService::jwks);
    }

    private Mono<Oauth2TokenResponse> checkAuthenticationResult(String clientId, Boolean result) {
        if (Boolean.TRUE.equals(result)) {
            final String token = tokenService.issue(clientId);
            return Mono.just(Oauth2TokenResponse.builder()
                    .accessToken(token)
                    .tokenType(TokenService.BEARER)
                    .expiresIn(TokenService.TOKEN_EXPIRES_IN_SECONDS)
                    .build());
        } else {
            return Mono.error(new BadCredentialsException("Invalid client_id or client_secret"));
        }
    }
}
