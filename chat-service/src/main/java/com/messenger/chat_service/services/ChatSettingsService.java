package com.messenger.chat_service.services;

import com.messenger.chat_service.dto.ChatSettingsDTO;
import com.messenger.chat_service.exceptions.ChatAccessDeniedException;
import com.messenger.chat_service.exceptions.ChatNotFoundException;
import com.messenger.chat_service.exceptions.ChatValidationException;
import com.messenger.chat_service.models.Chat;
import com.messenger.chat_service.models.ChatSettings;
import com.messenger.chat_service.models.enums.ChatRole;
import com.messenger.chat_service.models.enums.ChatType;
import com.messenger.chat_service.repositories.ChatRepository;
import com.messenger.chat_service.repositories.ChatSettingsRepository;
import com.messenger.chat_service.utils.MapperDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChatSettingsService {

    private final ChatSettingsRepository chatSettingsRepository;
    private final ChatRepository chatRepository;
    private final MapperDTO mapperDTO;

    public ChatSettingsDTO getChatSettings(int chatId, int userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        if (chat.getType() != ChatType.GROUP) {
            throw new ChatValidationException("Settings are only available for group chats");
        }

        boolean isMember = chat.getMembers().stream()
                .anyMatch(member -> member.getUserId() == userId);
        if (!isMember) {
            throw new ChatAccessDeniedException("User is not a member of this chat");
        }

        return mapperDTO.toChatSettingsDTO(chat.getSettings());
    }

    public void updateChatSettings(int chatId, int currentUserId, ChatSettingsDTO settings) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        validateGroupChat(chat);
        validateAdminRights(chat, currentUserId);

        ChatSettings chatSettings = chat.getSettings();
        chatSettings.setOnlyAdminsCanWrite(settings.isOnlyAdminsCanWrite());
        chatSettings.setOnlyAdminsCanAddMembers(settings.isOnlyAdminsCanAddMembers());
        chatSettings.setOnlyAdminsCanRemoveMembers(settings.isOnlyAdminsCanRemoveMembers());
        chatSettings.setOnlyAdminsCanChangeInfo(settings.isOnlyAdminsCanChangeInfo());

        chatRepository.save(chat);
    }

    private void validateGroupChat(Chat chat) {
        if (chat.getType() != ChatType.GROUP) {
            throw new IllegalStateException("This operation is only available for group chats");
        }
    }

    private void validateAdminRights(Chat chat, int userId) {
        boolean isAdmin = chat.getMembers().stream()
                .filter(member -> member.getUserId() == userId)
                .anyMatch(member -> member.getRole() == ChatRole.ADMIN || member.getRole() == ChatRole.CREATOR);
        if (!isAdmin) {
            throw new AccessDeniedException("Only admins can perform this operation");
        }
    }

}
