package com.messenger.presence_service.services;

import com.messenger.presence_service.dto.PresenceEventDTO;
import com.messenger.presence_service.dto.UserPresenceDTO;
import com.messenger.presence_service.models.EventType;
import com.messenger.presence_service.models.PresenceEvent;
import com.messenger.presence_service.models.PresenceStatus;
import com.messenger.presence_service.models.UserPresence;
import com.messenger.presence_service.utils.MapperDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PresenceService {

    private final RedisTemplate<String, UserPresence> userPresenceRedisTemplate;
    private final RedisTemplate<String, String> stringRedisTemplate;
    private final KafkaTemplate<String, PresenceEvent> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;
    private final MapperDTO mapperDTO;

    @Value("${presence.timeout.away}")
    private long awayTimeoutMs;

    @Value("${presence.timeout.offline}")
    private long offlineTimeoutMs;

    @Value("${kafka.topics.user-presence-events}")
    private String presenceEventsTopic;

    private static final String PRESENCE_KEY_PREFIX = "presence:";

    public UserPresence getUserPresence(int userId) {
        return userPresenceRedisTemplate.opsForValue().get(PRESENCE_KEY_PREFIX + userId);
    }

    public UserPresence updateUserStatus(int userId, PresenceStatus newStatus, String deviceId, String source) {
        UserPresence userPresence = getUserPresence(userId);
        PresenceStatus previousStatus = null;

        if (userPresence == null) {
            userPresence = new UserPresence();
            userPresence.setUserId(userId);
            userPresence.setLastActivity(Instant.now());
        } else {
            previousStatus = userPresence.getStatus();
        }

        userPresence.setStatus(newStatus);

        if (newStatus == PresenceStatus.ONLINE) {
            userPresence.updateLastActivity();
            if (deviceId != null) {
                userPresence.addDevice(deviceId);
            }
        } else if (newStatus == PresenceStatus.OFFLINE){
            userPresence.updateLastSeen();
            if (deviceId != null) {
                userPresence.removeDevice(deviceId);
            }
        }

        userPresenceRedisTemplate.opsForValue().set(PRESENCE_KEY_PREFIX + userId, userPresence);

        if (previousStatus == null || previousStatus != newStatus) {
            PresenceEvent event = new PresenceEvent();
            event.setEventType(EventType.STATUS_CHANGE);
            event.setUserId(userId);
            event.setDeviceId(deviceId);
            event.setPreviousStatus(previousStatus);
            event.setNewStatus(newStatus);
            event.setTimestamp(Instant.now());
            event.setSource(source);

            kafkaTemplate.send(presenceEventsTopic, String.valueOf(userId), event);

            messagingTemplate.convertAndSend("/topic/presence", mapperDTO.convertToPresenceEventDTO(event));
            messagingTemplate.convertAndSendToUser(String.valueOf(userId), "queue/presence", mapperDTO.convertToPresenceEventDTO(event));
        }

        return userPresence;
    }

    public UserPresence updateStatusMessage(int userId, String statusMessage) {
        UserPresence userPresence = getUserPresence(userId);

        if(userPresence == null) {
            userPresence = new UserPresence();
            userPresence.setUserId(userId);
            userPresence.setStatus(PresenceStatus.OFFLINE);
            userPresence.setLastActivity(Instant.now());
        }

        userPresence.setStatusMessage(statusMessage);

        userPresenceRedisTemplate.opsForValue().set(PRESENCE_KEY_PREFIX + userId, userPresence);

        PresenceEvent event = new PresenceEvent();
        event.setEventType(EventType.STATUS_MESSAGE_CHANGE);
        event.setUserId(userId);
        event.setStatusMessage(statusMessage);
        event.setTimestamp(Instant.now());
        event.setSource("USER");

        kafkaTemplate.send(presenceEventsTopic, String.valueOf(userId), event);

        messagingTemplate.convertAndSend("/topic/presence", mapperDTO.convertToPresenceEventDTO(event));
        messagingTemplate.convertAndSendToUser(String.valueOf(userId), "/queue/presence", mapperDTO.convertToPresenceEventDTO(event));

        return userPresence;
    }

    public List<Integer> getUsersByStatus(PresenceStatus status){
        Set<String> keys = userPresenceRedisTemplate.keys(PRESENCE_KEY_PREFIX + "*");

        if (keys == null || keys.isEmpty()) {
            return List.of();
        }

        return keys.stream()
                .map(key -> userPresenceRedisTemplate.opsForValue().get(key))
                .filter(presence -> presence != null && presence.getStatus() == status)
                .map(UserPresence::getUserId)
                .collect(Collectors.toList());
    }

    public List<UserPresenceDTO> getMultipleUsersPresence(List<Integer> userIds) {
        return userIds.stream()
                .map(this::getUserPresence)
                .filter(presence -> presence != null)
                .map(presence -> mapperDTO.convertToUserPresenceDTO(presence))
                .collect(Collectors.toList());
    }

    @Scheduled(fixedDelayString = "${presence.heartbeat.interval}")
    public void checkInactiveUsers() {
        Set<String> keys = userPresenceRedisTemplate.keys(PRESENCE_KEY_PREFIX + "*");
        if (keys == null || keys.isEmpty()) {
            return;
        }

        Instant now = Instant.now();

        for (String key : keys) {
            UserPresence presence = userPresenceRedisTemplate.opsForValue().get(key);

            if (presence == null || presence.getStatus() == PresenceStatus.OFFLINE
                    || presence.getStatus() == PresenceStatus.DO_NOT_DISTURB
                    || presence.getStatus() == PresenceStatus.INVISIBLE) {
                continue;
            }

            long inactivityMs = presence.getLastActivity() != null ?
                    ChronoUnit.MILLIS.between(presence.getLastActivity(), now) : Long.MAX_VALUE;

            if (presence.getStatus() == PresenceStatus.ONLINE && inactivityMs > awayTimeoutMs) {
                updateUserStatus(presence.getUserId(), PresenceStatus.AWAY, null, "SYSTEM");
            }

            else if (presence.getStatus() == PresenceStatus.AWAY && inactivityMs > offlineTimeoutMs) {
                updateUserStatus(presence.getUserId(), PresenceStatus.OFFLINE, null, "SYSTEM");
            }
        }
    }

}
