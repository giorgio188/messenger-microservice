package com.messenger.auth_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_devices")
@Data
@RequiredArgsConstructor
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "user_id")
    @NotNull
    private int userId;

    @Column(name = "device_details")
    @NotNull
    private String deviceDetails;  //fingerprint

    @Column
    private String deviceType;

    @Column
    private String osName;

    @Column
    private String osVersion;

    @Column
    private String browserName;

    @Column
    private String browserVersion;

    @Column(name = "device_name")
    @NotNull
    private String deviceName;

    @Column(name = "ip_address")
    @NotNull
    private String ipAddress;

    @Column(name = "last_login")
    @NotNull
    private LocalDateTime lastLogin;

    @Column(name = "is_trusted")
    @NotNull
    private boolean isTrusted = true;

    @Column(name = "created_at", updatable = false)
    @NotNull
    private LocalDateTime createdAt;

}
