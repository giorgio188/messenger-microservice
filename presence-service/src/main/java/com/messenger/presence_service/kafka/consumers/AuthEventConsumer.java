package com.messenger.presence_service.kafka.consumers;

import com.messenger.presence_service.models.AuthEvent;
import com.messenger.presence_service.models.PresenceStatus;
import com.messenger.presence_service.services.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthEventConsumer {

    private final PresenceService presenceService;

    @KafkaListener(topics = "${kafka.topics.auth-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeAuthEvent(AuthEvent authEvent) {
        switch (authEvent.getEventType()) {
            case LOGIN:
                handleLogin(authEvent);
                break;
            case LOGOUT:
                handleLogout(authEvent);
                break;
            case ACCOUNT_LOCKED:
                handleAccountLocked(authEvent);
                break;
            default:
        }
    }

    private void handleLogin(AuthEvent authEvent) {
        presenceService.updateUserStatus(
                authEvent.getUserId(),
                PresenceStatus.ONLINE,
                authEvent.getDeviceId(),
                "AUTH_SERVICE"
        );
    }

    private void handleLogout(AuthEvent authEvent) {
        presenceService.updateUserStatus(
                authEvent.getUserId(),
                PresenceStatus.OFFLINE,
                authEvent.getDeviceId(),
                "AUTH_SERVICE"
        );
    }

    private void handleAccountLocked(AuthEvent authEvent) {
        presenceService.updateUserStatus(
                authEvent.getUserId(),
                PresenceStatus.OFFLINE,
                null,
                "AUTH_SERVICE"
        );
    }

}
