package com.messenger.auth_service.dto;


import lombok.Data;

@Data
public class TokenPair {
    private String accessToken;
    private String refreshToken;
}
