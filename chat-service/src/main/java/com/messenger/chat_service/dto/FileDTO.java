package com.messenger.chat_service.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.messenger.chat_service.models.enums.FileType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class FileDTO {

    private int id;
    private int chatId;
    private int senderId;
    private String senderUsername;
    private String senderNickname;
    private String fileName;
    private String fileUrl;
    private long size;
    private FileType type;
    private int messageId;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime uploadedAt;
}
