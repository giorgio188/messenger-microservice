package com.messenger.message_service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.messenger.message_service.dto.UserProfileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class UserInfoUtil {

    private static final String PROFILE_CACHE_PREFIX = "user:profile:";
    private static final String EXISTS_CACHE_PREFIX = "user:exists:";
    private static final Duration CACHE_TTL = Duration.ofDays(30);

    private final WebClient.Builder webClientBuilder;
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public Mono<UserProfileDTO> getUserProfile(int userId) {
        String cacheKey = PROFILE_CACHE_PREFIX + userId;

        return redisTemplate.opsForValue().get(cacheKey)
                .map(json -> {
                    try {
                        return objectMapper.readValue(json, UserProfileDTO.class);
                    } catch (JsonProcessingException e) {
                        throw new RuntimeException("Cashed profile deserealization error", e);
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    return webClientBuilder.build()
                            .get()
                            .uri("/api/user/" + userId)
                            .retrieve()
                            .bodyToMono(UserProfileDTO.class)
                            .flatMap(profile -> {
                                try {
                                    String json = objectMapper.writeValueAsString(profile);
                                    return redisTemplate.opsForValue()
                                            .set(cacheKey, json, CACHE_TTL)
                                            .thenReturn(profile);
                                } catch (JsonProcessingException e) {
                                    return Mono.error(new RuntimeException("Profile serialization error", e));
                                }
                            });
                }));
    }

    public Mono<Boolean> ifUserExists(int userId) {
        String cacheKey = EXISTS_CACHE_PREFIX + userId;

        return redisTemplate.opsForValue().get(cacheKey)
                .map(value -> Boolean.parseBoolean(String.valueOf(value)))
                .switchIfEmpty(Mono.defer(() -> {
                    return webClientBuilder.build()
                            .get()
                            .uri("/api/user/exists/" + userId)
                            .retrieve()
                            .bodyToMono(Boolean.class)
                            .flatMap(exists ->
                                    redisTemplate.opsForValue()
                                            .set(cacheKey, String.valueOf(exists), CACHE_TTL)
                                            .thenReturn(exists)
                            );
                }));
    }
}
