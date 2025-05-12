package com.messenger.auth_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceDTO {
    private int id;
    private String deviceName;
    private LocalDateTime lastLoginDate;
    private String ipAddress;
    private boolean trusted;
    private boolean currentDevice;
}
