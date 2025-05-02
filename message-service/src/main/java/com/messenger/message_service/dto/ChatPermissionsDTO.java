package com.messenger.message_service.dto;

import lombok.Data;

import java.util.List;

@Data
public class ChatPermissionsDTO {
    private int chatId;
    private boolean onlyAdminsCanWrite;
    private List<Integer> mutedUserIds;
    private List<Integer> adminUserIds;
}
