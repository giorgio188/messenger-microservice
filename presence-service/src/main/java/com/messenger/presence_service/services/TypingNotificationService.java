package com.messenger.presence_service.services;

import com.messenger.presence_service.models.TypingEvent;
import com.messenger.presence_service.models.TypingEventType;
import com.messenger.presence_service.utils.MapperDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class TypingNotificationService {

    private final KafkaTemplate<String, TypingEvent> kafkaTemplate;
    private final SimpMessagingTemplate messagingTemplate;

    @Value("${kafka.topics.typing-notification-events}")
    private String typingNotificationTopic;

    @Value("${presence.typingStatus.expiry}")
    private long typingStatusExpiryMs;

    // Key is "userId:chatId", value is expiry time
    private final Map<String, Instant> activeTypingUsers = new ConcurrentHashMap<>();

    private final MapperDTO mapperDTO;

    public void processTypingStart(int userId, int chatId, String deviceId) {
        String key = userId + ":" + chatId;
        Instant expiryTime = Instant.now().plusMillis(typingStatusExpiryMs);

        activeTypingUsers.put(key, expiryTime);

        TypingEvent event = new TypingEvent();
        event.setEventType(TypingEventType.TYPING_START);
        event.setUserId(userId);
        event.setChatId(chatId);
        event.setTimestamp(Instant.now());
        event.setDeviceId(deviceId);

        kafkaTemplate.send(typingNotificationTopic, key, event);

        messagingTemplate.convertAndSend("/topic/chat." + chatId + ".typing", mapperDTO.convertToTypingEventDTO(event));

    }

    public void processTypingStop(int userId, int chatId, String deviceId) {
        String key = userId + ":" + chatId;

        activeTypingUsers.remove(key);

        TypingEvent event = new TypingEvent();
        event.setEventType(TypingEventType.TYPING_STOP);
        event.setUserId(userId);
        event.setChatId(chatId);
        event.setTimestamp(Instant.now());
        event.setDeviceId(deviceId);

        kafkaTemplate.send(typingNotificationTopic, key, event);

        messagingTemplate.convertAndSend("/topic/chat." + chatId + ".typing", mapperDTO.convertToTypingEventDTO(event));

    }

    @Scheduled(fixedRate = 5000) // Run every 5 seconds
    public void cleanupExpiredTypingNotifications() {
        Instant now = Instant.now();

        activeTypingUsers.entrySet().removeIf(entry -> {
            if (entry.getValue().isBefore(now)) {
                String[] parts = entry.getKey().split(":");
                int userId = Integer.parseInt(parts[0]);
                int chatId = Integer.parseInt(parts[1]);

                TypingEvent event = new TypingEvent();
                event.setEventType(TypingEventType.TYPING_STOP);
                event.setUserId(userId);
                event.setChatId(chatId);
                event.setTimestamp(now);
                event.setDeviceId("SYSTEM");

                kafkaTemplate.send(typingNotificationTopic, entry.getKey(), event);

                messagingTemplate.convertAndSend("/topic/chat." + chatId + ".typing", mapperDTO.convertToTypingEventDTO(event));


                return true;
            }
            return false;
        });
    }

}
