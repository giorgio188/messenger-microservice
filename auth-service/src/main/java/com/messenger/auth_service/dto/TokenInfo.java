package com.messenger.auth_service.dto;

import lombok.Data;

@Data
public class TokenInfo {
    private int userId;
    private long expirationTime;
}
