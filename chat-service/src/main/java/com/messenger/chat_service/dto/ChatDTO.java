package com.messenger.chat_service.dto;

import com.messenger.chat_service.models.enums.ChatType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChatDTO {
    private int id;
    private ChatType type;
    private String name;
    private String description;
    private String avatar;
    private int createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<ChatMemberDTO> members;
    private ChatSettingsDTO settings;
}
