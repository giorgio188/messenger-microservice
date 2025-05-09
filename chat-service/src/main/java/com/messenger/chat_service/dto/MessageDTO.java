package com.messenger.chat_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.messenger.chat_service.models.enums.MessageStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class MessageDTO {
    private int id;
    private int chatId;
    private int senderId;
    private String senderUsername;
    private String senderNickname;
    private String senderAvatar;
    private String content;
    private boolean isEdited;
    private boolean isDeleted;
    private MessageStatus status;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime sentAt;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime editedAt;

    private List<FileDTO> files;
}
