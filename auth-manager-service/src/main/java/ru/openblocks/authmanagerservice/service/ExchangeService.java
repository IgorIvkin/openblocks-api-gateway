package ru.openblocks.authmanagerservice.service;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.openblocks.authmanagerservice.api.dto.exchange.get.ExchangeTokenRequest;
import ru.openblocks.authmanagerservice.api.dto.token.get.Oauth2TokenResponse;
import ru.openblocks.authmanagerservice.exception.InvalidAccessTokenException;
import ru.openblocks.authmanagerservice.persistence.entity.AccessTokenEntity;
import ru.openblocks.authmanagerservice.persistence.repository.AccessTokenRepository;

import java.time.Instant;
import java.util.UUID;

/**
 * Service used to exchange access token to JWT-token.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExchangeService {

    private final TokenService tokenService;

    private final AccessTokenRepository accessTokenRepository;

    /**
     * Exchanges access token to JWT-token.
     * Issues a new JWT-token in case if the old one is already expired.
     *
     * @param request request with access token
     * @return JWT-token
     */
    public Oauth2TokenResponse exchange(ExchangeTokenRequest request) {

        final String accessToken = request.accessToken();

        if (tokenService.verifyAccessToken(accessToken)) {
            final DecodedJWT decodedToken = tokenService.decode(accessToken);
            final String uuid = decodedToken.getClaim("uuid").asString();

            log.info("Exchange access token with uuid {}", uuid);

            if (uuid != null) {
                return accessTokenRepository.get(UUID.fromString(uuid))
                        .map(this::getJwtTokenFromAccessToken)
                        .orElseThrow(() -> new InvalidAccessTokenException("No data found by access token"));
            } else {
                throw new InvalidAccessTokenException("Access token should contain claim \"uuid\"");
            }
        } else {
            throw new InvalidAccessTokenException("Access token is incorrect or expired");
        }
    }

    private Oauth2TokenResponse getJwtTokenFromAccessToken(AccessTokenEntity accessToken) {

        final Instant expiresIn = accessToken.getExpiresIn();
        if (tokenService.isExpired(expiresIn)) {
            throw new InvalidAccessTokenException("Access token is expired");
            // TODO: refresh access token here
        }

        final String oldJwtToken = accessToken.getJwtToken();

        if (tokenService.verifyJwtToken(oldJwtToken)) {
            final long secondsToExpire = tokenService.secondsToExpire(oldJwtToken);

            return Oauth2TokenResponse.builder()
                    .accessToken(oldJwtToken)
                    .tokenType(TokenService.BEARER)
                    .expiresIn((int) secondsToExpire)
                    .build();
        } else {
            final String clientId = accessToken.getClientId();
            final String newJwtToken = tokenService.issueJwt(clientId);

            return Oauth2TokenResponse.builder()
                    .accessToken(newJwtToken)
                    .tokenType(TokenService.BEARER)
                    .expiresIn(TokenService.TOKEN_EXPIRES_IN_SECONDS)
                    .build();
        }
    }
}
