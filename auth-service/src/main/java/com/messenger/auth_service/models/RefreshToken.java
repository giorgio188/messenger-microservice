package com.messenger.auth_service.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "refresh_tokens")
@Data
@RequiredArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "token_id", unique = true)
    @NotNull
    private String tokenId;  // JWT ID

    @Column(name = "token_hash")
    @NotNull
    private String token;    // Хэш токена

    @Column(name = "user_id")
    @NotNull
    private int userId;

    @Column(name = "device_id")
    @NotNull
    private int deviceId;   // ID устройства

    @Column(name = "expiry_date")
    @NotNull
    private Date expiryDate;

    @Column(name = "is_revoked")
    @NotNull
    private boolean revoked;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
