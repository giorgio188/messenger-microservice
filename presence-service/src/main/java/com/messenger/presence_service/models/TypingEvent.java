package com.messenger.presence_service.models;

import lombok.Data;

import java.time.Instant;

@Data
public class TypingEvent {

    private TypingEventType eventType;

    private int userId;

    private int chatId;

    private Instant timestamp = Instant.now();

    private String deviceId;

}
