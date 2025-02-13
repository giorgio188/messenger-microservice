package com.messenger.auth_service.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.messenger.auth_service.dto.TokenInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;


@Component
@RequiredArgsConstructor
public class JWTUtil {

    @Value("${spring.jwt.secret}")
    private String secret;

    public String generateToken(String username, int id) {
        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(6000).toInstant());
        return JWT.create()
                .withSubject("User authentication")
                .withClaim("id", id)
                .withClaim("username", username)
                .withIssuedAt(new Date())
                .withIssuer("admin")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret));
    }

    public Optional<String> verifyToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withSubject("User authentication")
                    .withIssuer("admin")
                    .build();

            DecodedJWT jwt = verifier.verify(token);
            String username = jwt.getClaim("username").asString();
            return Optional.of(username);
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }
    }

    public int extractUserId(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withSubject("User authentication")
                    .withIssuer("admin")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("id").asInt();
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    public TokenInfo extractTokenInfo(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withSubject("User authentication")
                    .withIssuer("admin")
                    .build();
            DecodedJWT jwt = verifier.verify(token);

            TokenInfo tokenInfo = new TokenInfo();
            tokenInfo.setUserId(jwt.getClaim("id").asInt());
            tokenInfo.setExpirationTime(jwt.getExpiresAt().toInstant().getEpochSecond());

            return tokenInfo;
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

}
