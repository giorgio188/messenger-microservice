package com.messenger.auth_service.dto;

import lombok.Data;

@Data
public class DeviceInfo {
    private String deviceId;       // Уникальный идентификатор устройства (fingerprint)
    private String deviceName;     // Название устройства (например, "iPhone 13", "Chrome on MacBook")
    private String deviceType;     // Тип устройства (мобильный, планшет, десктоп)
    private String osName;         // Название ОС
    private String osVersion;      // Версия ОС
    private String browserName;    // Название браузера (если применимо)
    private String browserVersion; // Версия браузера
    private String ipAddress;      // IP-адрес при входе
    private String userAgent;      // User-Agent строка
}
