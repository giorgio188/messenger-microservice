package com.messenger.group_chat_service.dto;

import lombok.Data;

@Data
public class GroupChatMembersDTO {
    private int memberId;
    private String username;
    private String nickname;
    private String role;
}
