package com.messenger.presence_service.dto;

import com.messenger.presence_service.models.PresenceStatus;
import lombok.Data;

import java.time.Instant;

@Data
public class PresenceEventDTO {

    private String eventType;

    private int userId;

    private String deviceId;

    private PresenceStatus previousStatus;

    private PresenceStatus newStatus;

    private String statusMessage;

    private Instant timestamp;

}
