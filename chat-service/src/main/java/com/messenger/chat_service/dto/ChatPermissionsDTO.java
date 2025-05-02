package com.messenger.chat_service.dto;

import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
public class ChatPermissionsDTO {
    private int chatId;
    private boolean onlyAdminsCanWrite;
    private List<Integer> mutedUserIds;
    private List<Integer> adminUserIds;
}
