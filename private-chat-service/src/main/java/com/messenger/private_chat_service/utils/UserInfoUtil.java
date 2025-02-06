package com.messenger.private_chat_service.utils;

import com.messenger.private_chat_service.dto.UserProfileDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
@RequiredArgsConstructor
public class UserInfoUtil {

    private final WebClient.Builder webClientBuilder;

    public UserProfileDTO getUserProfile(int userId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("/api/user/" + userId)
                    .retrieve()
                    .bodyToMono(UserProfileDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get user profile", e);
        }
    }

    public boolean ifUserExists(int userId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("/api/exists/" + userId)
                    .retrieve()
                    .bodyToMono(Boolean.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Failed to check user existence", e);
        }
    }
}
