package com.messenger.group_chat_service.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupChatMembersDTO {
    private int memberId;
    private String username;
    private String nickname;
    private String role;
    private LocalDateTime appliedAt;
}
