package ru.openblocks.authmanagerservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.openblocks.authmanagerservice.api.dto.checkauth.get.CheckAuthenticationRequest;
import ru.openblocks.authmanagerservice.api.dto.jwks.get.JwksResponse;
import ru.openblocks.authmanagerservice.api.dto.token.get.Oauth2TokenRequest;
import ru.openblocks.authmanagerservice.api.dto.token.get.Oauth2TokenResponse;
import ru.openblocks.authmanagerservice.exception.BadCredentialsException;
import ru.openblocks.authmanagerservice.persistence.repository.AccessTokenRepository;

import java.time.Instant;
import java.util.UUID;

/**
 * Service used to issue JWT- and access-tokens according to authentication of users.
 */
@Slf4j
@Service
public class AuthenticationService {

    private final TokenService tokenService;

    private final CheckAuthenticationService checkAuthenticationService;

    private final AccessTokenRepository accessTokenRepository;

    @Autowired
    public AuthenticationService(TokenService tokenService,
                                 CheckAuthenticationService checkAuthenticationService,
                                 AccessTokenRepository accessTokenRepository) {
        this.tokenService = tokenService;
        this.checkAuthenticationService = checkAuthenticationService;
        this.accessTokenRepository = accessTokenRepository;
    }

    /**
     * Validates user credentials and issues a token for user if everything is ok.
     * Throws 401 in case if credentials are invalid.
     *
     * @param request login and password of user
     * @return token in case credentials are ok
     */
    public Mono<Oauth2TokenResponse> getJwtToken(Oauth2TokenRequest request) {

        final String clientId = request.getClientId();
        final String clientSecret = request.getClientSecret();

        log.info("JWT: check credentials for client {}", clientId);

        final CheckAuthenticationRequest authRequest = CheckAuthenticationRequest.builder()
                .login(clientId)
                .password(clientSecret)
                .build();
        return checkAuthenticationService.checkAuthentication(authRequest)
                .flatMap(r -> issueJwtByAuthentication(clientId, r));
    }

    /**
     * Validates user credentials and issues an access-token if everything is ok.
     * Throws 401 in case if credentials are invalid.
     *
     * @param request login and password of user
     * @return token in case credentials are ok
     */
    public Mono<Oauth2TokenResponse> getAccessToken(Oauth2TokenRequest request) {

        final String clientId = request.getClientId();
        final String clientSecret = request.getClientSecret();
        final UUID uuid = UUID.randomUUID();

        log.info("Access: check credentials for client {}", clientId);

        final CheckAuthenticationRequest authRequest = CheckAuthenticationRequest.builder()
                .login(clientId)
                .password(clientSecret)
                .build();
        return checkAuthenticationService.checkAuthentication(authRequest)
                .flatMap(r -> issueAccessByAuthentication(uuid, r))
                .doOnNext(token -> storeAccessToken(uuid, clientId));
    }

    /**
     * Returns current JWK for JWKs endpoint.
     *
     * @return current JWK as JWKS
     */
    public Mono<JwksResponse> getJwks() {
        return Mono.fromSupplier(tokenService::jwks);
    }

    private Mono<Oauth2TokenResponse> issueJwtByAuthentication(String clientId, Boolean result) {
        if (Boolean.TRUE.equals(result)) {
            final String token = tokenService.issueJwt(clientId);
            return Mono.just(Oauth2TokenResponse.builder()
                    .accessToken(token)
                    .tokenType(TokenService.BEARER)
                    .expiresIn(TokenService.TOKEN_EXPIRES_IN_SECONDS)
                    .build());
        } else {
            return Mono.error(new BadCredentialsException("Invalid client_id or client_secret"));
        }
    }

    private Mono<Oauth2TokenResponse> issueAccessByAuthentication(UUID uuid, Boolean result) {
        if (Boolean.TRUE.equals(result)) {
            log.info("Issue access token with uuid: {}", uuid);
            final String token = tokenService.issueAccess(uuid);

            return Mono.just(Oauth2TokenResponse.builder()
                    .accessToken(token)
                    .tokenType(TokenService.BEARER)
                    .expiresIn(TokenService.TOKEN_EXPIRES_IN_SECONDS)
                    .build());
        } else {
            return Mono.error(new BadCredentialsException("Invalid client_id or client_secret"));
        }
    }

    private void storeAccessToken(UUID uuid, String clientId) {
        final Instant expiresIn = tokenService.getExpiresIn();
        accessTokenRepository.store(
                uuid,
                clientId,
                tokenService.getExpiresIn(),
                tokenService.issueJwt(clientId, expiresIn)
        );
    }
}
