package com.messenger.auth_service.dto;

import lombok.Data;

@Data
public class DeviceInfo {
    private String deviceId;              // Уникальный идентификатор устройства
    private String deviceName;            // Название устройства
    private String deviceType;            // Тип устройства
    private String deviceBrand;           // Бренд устройства
    private String deviceModel;           // Модель устройства
    private String osName;                // Название ОС
    private String osVersion;             // Версия ОС
    private String browserName;           // Название браузера
    private String browserVersion;        // Версия браузера
    private String browserEngine;         // Движок браузера
    private String userAgent;             // Полная User-Agent строка
    private String ipAddress;             // IP-адрес при входе

    // Дополнительные поля для более точной идентификации
    private boolean isMobile;             // Является ли устройство мобильным
    private boolean isTablet;             // Является ли устройство планшетом
    private boolean isDesktop;            // Является ли устройство настольным компьютером
    private String layoutEngine;          // Название и версия движка рендеринга
    private String networkType;           // Тип сети, если известен
    private String screenResolution;      // Разрешение экрана, если известно
}
