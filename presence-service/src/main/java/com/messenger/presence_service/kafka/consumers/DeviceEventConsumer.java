package com.messenger.presence_service.kafka.consumers;

import com.messenger.presence_service.models.DeviceEvent;
import com.messenger.presence_service.models.PresenceStatus;
import com.messenger.presence_service.models.UserPresence;
import com.messenger.presence_service.services.PresenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceEventConsumer {

    private final PresenceService presenceService;


    @KafkaListener(topics = "${kafka.topics.device-status-events}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumeDeviceEvent(DeviceEvent deviceEvent) {

        switch (deviceEvent.getEventType()) {
            case DEVICE_ADDED:
                handleDeviceAdded(deviceEvent);
                break;
            case DEVICE_REMOVED:
                handleDeviceRemoved(deviceEvent);
                break;
            default:
        }
    }

    private void handleDeviceAdded(DeviceEvent deviceEvent) {
        UserPresence userPresence = presenceService.getUserPresence(deviceEvent.getUserId());

        if (userPresence == null) {
            presenceService.updateUserStatus(
                    deviceEvent.getUserId(),
                    PresenceStatus.OFFLINE,
                    deviceEvent.getDeviceId(),
                    "DEVICE_SERVICE"
            );
        } else {
            userPresence.addDevice(deviceEvent.getDeviceId());
            presenceService.updateUserStatus(
                    deviceEvent.getUserId(),
                    userPresence.getStatus(),
                    deviceEvent.getDeviceId(),
                    "DEVICE_SERVICE"
            );
        }
    }

    private void handleDeviceRemoved(DeviceEvent deviceEvent) {
        UserPresence userPresence = presenceService.getUserPresence(deviceEvent.getUserId());

        if (userPresence != null) {
            userPresence.removeDevice(deviceEvent.getDeviceId());

            if (!userPresence.hasActiveDevices() &&
                    (userPresence.getStatus() == PresenceStatus.ONLINE ||
                            userPresence.getStatus() == PresenceStatus.AWAY)) {

                presenceService.updateUserStatus(
                        deviceEvent.getUserId(),
                        PresenceStatus.OFFLINE,
                        null,
                        "DEVICE_SERVICE"
                );
            } else {
                presenceService.updateUserStatus(
                        deviceEvent.getUserId(),
                        userPresence.getStatus(),
                        null,
                        "DEVICE_SERVICE"
                );
            }
        }
    }

}
