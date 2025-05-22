package com.messenger.auth_service.models;

import lombok.Data;

@Data
public class AuthEvent {
    private EventType eventType;
    private int userId;
    private String deviceId;
    private long timestamp = System.currentTimeMillis();
}
