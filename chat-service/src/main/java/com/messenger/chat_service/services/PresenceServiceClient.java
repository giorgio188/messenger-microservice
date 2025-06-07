package com.messenger.chat_service.services;


import com.messenger.chat_service.dto.UserPresenceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresenceServiceClient {

    private final RestTemplate restTemplate;

    @Value("${services.presence.url}")
    private String presenceServiceUrl;

    public UserPresenceDTO getUserPresence(int userId) {
        try {
            String url = presenceServiceUrl + "/api/presence/{userId}";
            ResponseEntity<UserPresenceDTO> response = restTemplate.getForEntity(
                    url,
                    UserPresenceDTO.class,
                    userId
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                return response.getBody();
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public List<UserPresenceDTO> getMultipleUsersPresence(List<Integer> userIds) {
        try {
            if (userIds == null || userIds.isEmpty()) {
                return List.of();
            }

            String idsParam = userIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));

            String url = presenceServiceUrl + "/api/presence/bulk?userIds={ids}";

            ResponseEntity<List<UserPresenceDTO>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<List<UserPresenceDTO>>() {},
                    idsParam
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            } else {
                return List.of();
            }
        } catch (Exception e) {
            return List.of();
        }
    }

    public boolean isPresenceServiceAvailable() {
        try {
            String healthUrl = presenceServiceUrl + "/actuator/health";
            ResponseEntity<String> response = restTemplate.getForEntity(healthUrl, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
