package com.messenger.group_chat_service.dto;


import lombok.Data;

@Data
public class UserProfileDTO {
    private int id;
    private String username;
    private String nickname;
    private String avatar;
}
