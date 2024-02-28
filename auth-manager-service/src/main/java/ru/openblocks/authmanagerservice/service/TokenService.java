package ru.openblocks.authmanagerservice.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.JWK;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.openblocks.authmanagerservice.api.dto.jwks.get.JwksResponse;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.KeyPair;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.sql.Timestamp;
import java.time.*;
import java.util.List;
import java.util.UUID;

/**
 * Service used to verify JWT-tokens, parse them and to issue a new tokens
 * for users of application.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    public static final String BEARER = "Bearer";

    public static final String TOKEN_ISSUER = "openblocks-api-gateway";

    public static final int TOKEN_EXPIRES_IN_SECONDS = 3600;

    @Value("${app.jwt.private-key}")
    private String privateKey;

    @Value("${app.jwt.public-key}")
    private String publicKey;

    private Algorithm algorithm;

    private JWTVerifier jwtVerifier;

    private JWTVerifier accessVerifier;

    private JWK jwk;

    private final Clock clock = Clock.systemDefaultZone();

    @PostConstruct
    public void initialize() throws IOException {
        try (Reader stringReader = new StringReader(privateKey);
             PEMParser pemParser = new PEMParser(stringReader)) {
            PEMKeyPair pemKeyPair = (PEMKeyPair) pemParser.readObject();

            JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            KeyPair keyPair = converter.getKeyPair(pemKeyPair);

            ECPrivateKey ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();
            ECPublicKey ecPublicKey = (ECPublicKey) keyPair.getPublic();

            algorithm = Algorithm.ECDSA512(ecPublicKey, ecPrivateKey);

            jwtVerifier = JWT.require(algorithm)
                    .withIssuer(TOKEN_ISSUER)
                    // accept tokens that expire in 1 minute
                    .acceptExpiresAt(60)
                    .build();

            accessVerifier = JWT.require(algorithm)
                    .withClaimPresence("uuid")
                    .build();

            jwk = new ECKey.Builder(Curve.P_521, ecPublicKey)
                    .privateKey(ecPrivateKey)
                    .build();

            log.info("JWT token service initialization is finished");
        }
    }

    /**
     * Generates JWT-token for user with lifetime now + one month.
     *
     * @param userName name of user
     * @return JWT-token
     */
    public String issueJwt(final String userName) {
        return issueJwt(userName, null);
    }

    /**
     * Generates JWT-token for user with lifetime now + one month.
     *
     * @param userName  name of user
     * @param expiresIn time to expire a token
     * @return JWT-token
     */
    public String issueJwt(final String userName,
                           final Instant expiresIn) {
        final LocalDateTime now = LocalDateTime.now(clock);
        final Timestamp expiresAt;
        if (expiresIn == null) {
            expiresAt = Timestamp.valueOf(now.plusSeconds(TOKEN_EXPIRES_IN_SECONDS));
        } else {
            expiresAt = Timestamp.valueOf(LocalDateTime.ofInstant(expiresIn, ZoneId.systemDefault()));
        }
        return JWT.create()
                .withIssuer(TOKEN_ISSUER)
                .withSubject(userName)
                .withIssuedAt(Timestamp.valueOf(now))
                .withExpiresAt(expiresAt)
                .sign(algorithm);
    }

    /**
     * Generates an access-token for user.
     *
     * @param uuid access-token uuid
     * @return access-token
     */
    public String issueAccess(final UUID uuid) {
        return JWT.create().withClaim("uuid", uuid.toString()).sign(algorithm);
    }

    /**
     * Refresh token returning a new token with increased lifetime.
     * Returns the same token in case if it is not expired.
     *
     * @param token JWT-token to refresh
     * @return new JWT-token
     */
    public String refreshJwt(final String token) {
        final DecodedJWT decodedJwt = decode(token);
        try {
            jwtVerifier.verify(decodedJwt);
            return token;
        } catch (TokenExpiredException ex) {
            return issueJwt(decodedJwt.getSubject());
        }
    }

    /**
     * Verifies JWT-token. Returns false in case if token is wrong, signed by wrong key
     * or in case if it's expired.
     *
     * @param token JWT-token to analyze
     * @return true, if token is valid
     */
    public boolean verifyJwtToken(final String token) {
        if (token != null) {
            try {
                jwtVerifier.verify(token);
                return true;
            } catch (TokenExpiredException ex) {
                log.error("JWT-token was expired", ex);
                return false;
            } catch (JWTVerificationException ex) {
                log.error("JWT-token verification error", ex);
                return false;
            }
        }
        return false;
    }

    /**
     * Verifies access token. Returns false in case if token is wrong, signed by wrong key
     * or in case if it's expired.
     *
     * @param token JWT-token to analyze
     * @return true, if token is valid
     */
    public boolean verifyAccessToken(final String token) {
        if (token != null) {
            try {
                accessVerifier.verify(token);
                return true;
            } catch (TokenExpiredException ex) {
                log.error("Access token was expired", ex);
                return false;
            } catch (JWTVerificationException ex) {
                log.error("Access token verification error", ex);
                return false;
            }
        }
        return false;
    }

    /**
     * Decodes token parsing it to claims. This method is used to parse token without verification.
     * Useful when token is already expired.
     *
     * @param token JWT-token to decode
     * @return decoded token claims
     */
    public DecodedJWT decode(final String token) {
        if (token != null) {
            return JWT.decode(token);
        }
        return null;
    }

    /**
     * Returns seconds to expire between expiration time in token and current time.
     *
     * @param token token
     * @return seconds to expire token
     */
    public long secondsToExpire(final String token) {
        final DecodedJWT decodedToken = decode(token);
        return Duration.between(
                Instant.now(clock),
                decodedToken.getExpiresAtAsInstant()
        ).toSeconds();
    }

    /**
     * Returns true if token expired.
     *
     * @param tokenExpiresIn instant to expire the token
     * @return true if token is expired
     */
    public boolean isExpired(Instant tokenExpiresIn) {
        return Instant.now(clock).isAfter(tokenExpiresIn);
    }

    /**
     * Returns standard expire time for token.
     *
     * @return expiration time for token
     */
    public Instant getExpiresIn() {
        return Instant.now(clock).plusSeconds(TOKEN_EXPIRES_IN_SECONDS);
    }

    /**
     * Returns current JWK for JWKs endpoint.
     *
     * @return current JWK as JWKS
     */
    public JwksResponse jwks() {
        return JwksResponse.builder()
                .keys(List.of(jwk.toJSONObject()))
                .build();
    }
}
