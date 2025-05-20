package com.messenger.auth_service.dto;

import lombok.Data;

import java.util.Date;

@Data
public class TokenInfo {
    private int userId;
    private Date expirationTime;
    private int deviceId;
}
