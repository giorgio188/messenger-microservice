package com.messenger.group_chat_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class GroupChatMessageDTO {
    private int id;
    private int groupChatId;
    private int senderId;
    private String senderUsername;
    private String senderNickname;
    private String message;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sendTime;
}
