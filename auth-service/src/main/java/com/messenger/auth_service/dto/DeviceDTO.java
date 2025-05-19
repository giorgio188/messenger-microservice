package com.messenger.auth_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DeviceDTO {
    private int id;
    private String deviceName;
    private String deviceType;
    private String osName;
    private String osVersion;
    private String browserName;
    private String browserVersion;
    private String ipAddress;
    private LocalDateTime lastLoginDate;
    private boolean trusted;
    private boolean currentDevice;

    private String formattedLastLoginDate;
}
