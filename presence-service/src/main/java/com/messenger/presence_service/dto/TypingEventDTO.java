package com.messenger.presence_service.dto;

import lombok.Data;

import java.time.Instant;

@Data
public class TypingEventDTO {

    private String eventType;

    private int userId;

    private int chatId;

    private Instant timestamp;

}
