package com.messenger.presence_service.models;

import lombok.Data;

import java.time.Instant;

@Data
public class PresenceEvent {

    private EventType eventType;

    private int userId;

    private String deviceId;

    private PresenceStatus previousStatus;

    private PresenceStatus newStatus;

    private String statusMessage;

    private Instant timestamp = Instant.now();

    private String source;
}
