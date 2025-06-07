package com.messenger.presence_service.events;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserDisconnectEvent extends ApplicationEvent {
    private final int userId;
    private final String deviceId;

    public UserDisconnectEvent(Object source, int userId, String deviceId) {
        super(source);
        this.userId = userId;
        this.deviceId = deviceId;
    }
}
