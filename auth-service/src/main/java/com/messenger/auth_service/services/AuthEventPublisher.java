package com.messenger.auth_service.services;

import com.messenger.auth_service.models.AuthEvent;
import com.messenger.auth_service.models.EventType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthEventPublisher {

    private final KafkaTemplate<String, AuthEvent> kafkaTemplate;


    @Value("${kafka.topics.auth-events}")
    private String authEventsTopic;

    public void publishLoginEvent(int userId, String deviceId) {
        AuthEvent event = new AuthEvent();
        event.setEventType(EventType.LOGIN);
        event.setUserId(userId);
        event.setDeviceId(deviceId);

        kafkaTemplate.send(authEventsTopic, String.valueOf(userId), event);

    }

    public void publishLogoutEvent(int userId, String deviceId) {
        AuthEvent event = new AuthEvent();
        event.setEventType(EventType.LOGOUT);
        event.setUserId(userId);
        event.setDeviceId(deviceId);

        kafkaTemplate.send(authEventsTopic, String.valueOf(userId), event);

    }
}
