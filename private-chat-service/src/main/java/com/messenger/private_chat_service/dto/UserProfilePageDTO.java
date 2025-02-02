package com.messenger.private_chat_service.dto;

import lombok.Data;

@Data
public class UserProfilePageDTO {
    private String username;
    private String nickname;
    private String phoneNumber;
    private String email;
    private String avatar;
}
