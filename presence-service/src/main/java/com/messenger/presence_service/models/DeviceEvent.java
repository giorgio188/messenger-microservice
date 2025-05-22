package com.messenger.presence_service.models;

import lombok.Data;

@Data
public class DeviceEvent {
    private DeviceEventType eventType;
    private int userId;
    private String deviceId;
    private long timestamp;
}
