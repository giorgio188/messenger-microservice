package com.messenger.chat_service.dto;

import lombok.Data;

@Data
public class ChatSettingsDTO {
    private boolean onlyAdminsCanWrite;
    private boolean onlyAdminsCanAddMembers;
    private boolean onlyAdminsCanRemoveMembers;
    private boolean onlyAdminsCanChangeInfo;
}
