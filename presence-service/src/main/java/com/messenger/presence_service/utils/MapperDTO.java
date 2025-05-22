package com.messenger.presence_service.utils;

import com.messenger.presence_service.dto.PresenceEventDTO;
import com.messenger.presence_service.dto.TypingEventDTO;
import com.messenger.presence_service.dto.UserPresenceDTO;
import com.messenger.presence_service.models.PresenceEvent;
import com.messenger.presence_service.models.TypingEvent;
import com.messenger.presence_service.models.UserPresence;
import org.springframework.stereotype.Component;

@Component
public class MapperDTO {

    public UserPresenceDTO convertToUserPresenceDTO(UserPresence userPresence) {
        UserPresenceDTO dto = new UserPresenceDTO();
        dto.setUserId(userPresence.getUserId());
        dto.setStatus(userPresence.getStatus());
        dto.setLastActivity(userPresence.getLastActivity());
        dto.setLastSeen(userPresence.getLastSeen());
        dto.setStatusMessage(userPresence.getStatusMessage());
        return dto;
    }

    public PresenceEventDTO convertToPresenceEventDTO(PresenceEvent event) {
        PresenceEventDTO dto = new PresenceEventDTO();
        dto.setEventType(event.getEventType().toString());
        dto.setUserId(event.getUserId());
        dto.setDeviceId(event.getDeviceId());
        dto.setPreviousStatus(event.getPreviousStatus());
        dto.setNewStatus(event.getNewStatus());
        dto.setStatusMessage(event.getStatusMessage());
        dto.setTimestamp(event.getTimestamp());
        return dto;
    }

    public TypingEventDTO convertToTypingEventDTO(TypingEvent event) {
        TypingEventDTO dto = new TypingEventDTO();
        dto.setEventType(event.getEventType().toString());
        dto.setUserId(event.getUserId());
        dto.setChatId(event.getChatId());
        dto.setTimestamp(event.getTimestamp());
        return dto;
    }
}
