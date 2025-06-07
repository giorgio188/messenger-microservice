package com.messenger.chat_service.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class UserPresenceDTO {
    private int userId;
    private String status;
    private Instant lastActivity;
    private Instant lastSeen;
    private String statusMessage;
}
