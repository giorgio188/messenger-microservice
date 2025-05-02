package com.messenger.message_service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.messenger.message_service.dto.ChatPermissionsDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class ChatAccessUtil {
    private final WebClient.Builder webClientBuilder;
    private static final String ACCESS_CACHE_PREFIX = "chat:access:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(5);
    private static final String CHAT_PERMISSIONS_PREFIX = "chat:permissions:";
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;


    public boolean hasUserAccessToChat(int userId, int chatId) {
        String cacheKey = ACCESS_CACHE_PREFIX + chatId + ":" + userId;

        try {
            Boolean hasAccess = redisTemplate.opsForValue().get(cacheKey)
                    .map(Boolean::parseBoolean)
                    .switchIfEmpty(Mono.defer(() -> {
                        return webClientBuilder.build()
                                .get()
                                //TODO сделать метод в чат сервисе
                                .uri("/api/chat/{chatId}/member/check?userId={userId}", chatId, userId)
                                .header("X-User-Id", String.valueOf(userId))
                                .retrieve()
                                .bodyToMono(Boolean.class)
                                .flatMap(result -> {
                                    return redisTemplate.opsForValue()
                                            .set(cacheKey, String.valueOf(result), CACHE_TTL)
                                            .thenReturn(result);
                                })
                                .onErrorReturn(false);
                    }))
                    .block();

            return hasAccess != null && hasAccess;
        } catch (Exception e) {
            return false;
        }
    }

    public ChatPermissionsDTO getChatPermissions(int userId, int chatId) {
        String cacheKey = CHAT_PERMISSIONS_PREFIX + chatId;

        try {
            return redisTemplate.opsForValue().get(cacheKey)
                    .flatMap(json -> {
                        try {
                            return Mono.just(objectMapper.readValue(json, ChatPermissionsDTO.class));
                        } catch (JsonProcessingException e) {
                            return Mono.empty();
                        }
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        return webClientBuilder.build()
                                .get()
                                .uri("/api/chat/{chatId}/permissions", chatId)
                                .header("X-User-Id", String.valueOf(userId))
                                .retrieve()
                                .bodyToMono(ChatPermissionsDTO.class)
                                .flatMap(permissions -> {
                                    try {
                                        String json = objectMapper.writeValueAsString(permissions);
                                        return redisTemplate.opsForValue()
                                                .set(cacheKey, json, CACHE_TTL)
                                                .thenReturn(permissions);
                                    } catch (JsonProcessingException e) {
                                        return Mono.just(permissions);
                                    }
                                });
                    }))
                    .onErrorResume(e -> {
                        throw new RuntimeException("Can't parse chat permissions", e);
                    })
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Can't parse chat permissions", e);
        }
    }

    public boolean canUserWriteToChat(int userId, int chatId) {
        ChatPermissionsDTO permissions = getChatPermissions(userId, chatId);

        if (permissions.getMutedUserIds().contains(userId)) {
            return false;
        }

        if (permissions.isOnlyAdminsCanWrite()) {
            return permissions.getAdminUserIds().contains(userId);
        }

        return true;
    }

}
