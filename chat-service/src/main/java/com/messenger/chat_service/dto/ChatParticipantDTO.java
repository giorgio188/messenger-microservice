package com.messenger.chat_service.dto;

import lombok.Data;

@Data
public class ChatParticipantDTO {
    private int id;
    private String username;
    private String nickname;
    private String avatar;
}
