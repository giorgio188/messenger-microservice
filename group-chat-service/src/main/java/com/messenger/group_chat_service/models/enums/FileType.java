package com.messenger.group_chat_service.models.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.Optional;

@Getter
public enum FileType {
    JPEG("image/jpeg", ".jpg"),
    PNG("image/png", ".png"),
    PDF("application/pdf", ".pdf"),
    DOC("application/msword", ".doc"),
    DOCX("application/vnd.openxmlformats-officedocument.wordprocessingml.document", ".docx"),
    TXT("text/plain", ".txt"),
    ZIP("application/zip", ".zip"),
    RAR("application/x-rar-compressed", ".rar");

    private final String contentType;
    @Getter
    private final String extension;

    FileType(String contentType, String extension) {
        this.contentType = contentType;
        this.extension = extension;
    }

    public static Optional<FileType> getByContentType(String contentType) {
        return Arrays.stream(values())
                .filter(type -> type.getContentType().equals(contentType))
                .findFirst();
    }

}
