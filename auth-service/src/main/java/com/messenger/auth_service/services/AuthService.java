package com.messenger.auth_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final ReactiveRedisTemplate<String, Integer> redisTemplate;
    private static final String CACHE_PREFIX = "auth:";

    public void logout(String token) {
        String tokenHash = hashToken(token);
        if (tokenHash == null) {
            throw new RuntimeException("Error processing token");
        }
        redisTemplate.delete(tokenHash);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return CACHE_PREFIX + Base64.getUrlEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }
}
