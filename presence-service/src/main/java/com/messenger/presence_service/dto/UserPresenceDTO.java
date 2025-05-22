package com.messenger.presence_service.dto;

import com.messenger.presence_service.models.PresenceStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class UserPresenceDTO {

    private int userId;

    private PresenceStatus status;

    private Instant lastActivity;

    private Instant lastSeen;

    private String statusMessage;

}
