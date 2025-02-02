package com.messenger.private_chat_service.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class MultipartFileWrapper {
    private int chatId;
    private MultipartFile file;
}
