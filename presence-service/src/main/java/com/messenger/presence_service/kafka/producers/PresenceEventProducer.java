package com.messenger.presence_service.kafka.producers;

import com.messenger.presence_service.models.PresenceEvent;
import com.messenger.presence_service.models.TypingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PresenceEventProducer {

    private final KafkaTemplate<String, PresenceEvent> presenceEventTemplate;
    private final KafkaTemplate<String, TypingEvent> typingEventTemplate;

    @Value("${kafka.topics.user-presence-events}")
    private String presenceEventsTopic;

    @Value("${kafka.topics.typing-notification-events}")
    private String typingEventsTopic;


    public void sendPresenceEvent(PresenceEvent event) {
        presenceEventTemplate.send(presenceEventsTopic, String.valueOf(event.getUserId()), event);
    }

    public void sendTypingEvent(TypingEvent event) {
        String key = event.getUserId() + ":" + event.getChatId();
        typingEventTemplate.send(typingEventsTopic, key, event);
    }

}
