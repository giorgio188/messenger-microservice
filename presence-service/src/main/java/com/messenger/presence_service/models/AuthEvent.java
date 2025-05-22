package com.messenger.presence_service.models;

import lombok.Data;

@Data
public class AuthEvent {
    private AuthEventType eventType;
    private int userId;
    private String deviceId;
    private long timestamp;
}
