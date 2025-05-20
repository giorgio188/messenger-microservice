package com.messenger.auth_service.services;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final ReactiveRedisTemplate<String, Integer> redisTemplate;

    public void markDeviceAsRevoked(String deviceRevokedKey, long ttlMillis) {
        if (ttlMillis > 0) {
            redisTemplate.opsForValue().set(
                    deviceRevokedKey,
                    1,
                    Duration.ofMillis(ttlMillis)
            ).subscribe();
        }
    }


    public boolean isDeviceRevoked(int userId, long deviceId) {
        String deviceRevokedKey = String.format("revoked_device:%d:%d", userId, deviceId);
        return Boolean.TRUE.equals(
                redisTemplate.hasKey(deviceRevokedKey).block()
        );
    }
}
