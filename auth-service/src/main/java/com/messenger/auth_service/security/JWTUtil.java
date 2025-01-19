package com.messenger.auth_service.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.Date;




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

    public String verifyToken(String token) throws JWTVerificationException {
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                .withSubject("User authentication")
                .withIssuer("admin")
                .build();
        DecodedJWT jwt = verifier.verify(token);
        return jwt.getClaim("username").asString();
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

}
