package com.messenger.presence_service.services;

import com.messenger.presence_service.events.UserConnectEvent;
import com.messenger.presence_service.events.UserDisconnectEvent;
import com.messenger.presence_service.models.EventType;
import com.messenger.presence_service.models.PresenceEvent;
import com.messenger.presence_service.models.PresenceStatus;
import com.messenger.presence_service.models.UserPresence;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserSessionService {

    private final PresenceService presenceService;
    private final KafkaTemplate<String, PresenceEvent> kafkaTemplate;

    @EventListener
    public void handleUserConnectEvent(UserConnectEvent event) {
;
        handleUserConnect(event.getUserId(), event.getDeviceId());
    }

    @EventListener
    public void handleUserDisconnectEvent(UserDisconnectEvent event) {

        handleUserDisconnect(event.getUserId(), event.getDeviceId());
    }

    private void handleUserConnect(int userId, String deviceId) {

        UserPresence userPresence = presenceService.updateUserStatus(userId, PresenceStatus.ONLINE, deviceId, "WEBSOCKET");

        // If this is a new device connection, send DEVICE_CONNECTED event
        if (deviceId != null && userPresence.getActiveDevices().contains(deviceId)) {
            PresenceEvent deviceEvent = new PresenceEvent();
            deviceEvent.setEventType(EventType.DEVICE_CONNECTED);
            deviceEvent.setUserId(userId);
            deviceEvent.setDeviceId(deviceId);
            deviceEvent.setTimestamp(java.time.Instant.now());
            deviceEvent.setSource("WEBSOCKET");

            kafkaTemplate.send("device-status-events", String.valueOf(userId), deviceEvent);
        }
    }

    private void handleUserDisconnect(int userId, String deviceId) {

        UserPresence userPresence = presenceService.getUserPresence(userId);

        if (userPresence == null) {
            return;
        }

        if (deviceId != null) {
            userPresence.removeDevice(deviceId);

            PresenceEvent deviceEvent = new PresenceEvent();
            deviceEvent.setEventType(EventType.DEVICE_DISCONNECTED);
            deviceEvent.setUserId(userId);
            deviceEvent.setDeviceId(deviceId);
            deviceEvent.setTimestamp(java.time.Instant.now());
            deviceEvent.setSource("WEBSOCKET");

            kafkaTemplate.send("device-status-events", String.valueOf(userId), deviceEvent);
        }

        if (!userPresence.hasActiveDevices()) {
            presenceService.updateUserStatus(userId, PresenceStatus.OFFLINE, null, "WEBSOCKET");
        }
    }

    public void processHeartbeat(int userId, String deviceId) {

        UserPresence userPresence = presenceService.getUserPresence(userId);

        if (userPresence != null) {
            userPresence.updateLastActivity();

            if (userPresence.getStatus() != PresenceStatus.ONLINE &&
                    userPresence.hasActiveDevices() &&
                    userPresence.getStatus() != PresenceStatus.DO_NOT_DISTURB &&
                    userPresence.getStatus() != PresenceStatus.INVISIBLE) {

                presenceService.updateUserStatus(userId, PresenceStatus.ONLINE, deviceId, "HEARTBEAT");
            }
        }
    }

}
