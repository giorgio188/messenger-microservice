package com.messenger.chat_service.dto;

import com.messenger.chat_service.models.enums.ChatRole;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ChatMemberDTO {
    private int userId;
    private String username;
    private String nickname;
    private String avatar;
    private ChatRole role;
    private LocalDateTime joinedAt;
    private LocalDateTime lastReadAt;
}
