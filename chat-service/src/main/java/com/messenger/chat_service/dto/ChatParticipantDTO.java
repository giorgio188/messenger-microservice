package com.messenger.chat_service.dto;

import lombok.Data;

@Data
public class ChatParticipantDTO {
    private int userId;
    private String username;
    private String nickname;
    private String avatar;
}
