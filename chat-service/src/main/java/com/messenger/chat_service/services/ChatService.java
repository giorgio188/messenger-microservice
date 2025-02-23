package com.messenger.chat_service.services;

import com.messenger.chat_service.dto.ChatDTO;
import com.messenger.chat_service.dto.ChatMemberDTO;
import com.messenger.chat_service.dto.GroupChatCreationDTO;
import com.messenger.chat_service.exceptions.ChatNotFoundException;
import com.messenger.chat_service.exceptions.UserNotFoundException;
import com.messenger.chat_service.models.Chat;
import com.messenger.chat_service.models.ChatMember;
import com.messenger.chat_service.models.ChatSettings;
import com.messenger.chat_service.models.enums.ChatRole;
import com.messenger.chat_service.models.enums.ChatType;
import com.messenger.chat_service.repositories.ChatRepository;
import com.messenger.chat_service.utils.MapperDTO;
import com.messenger.chat_service.utils.UserInfoUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {
    private final S3Service s3Service;
    private final MapperDTO mapperDTO;
    private final ChatRepository chatRepository;
    private final UserInfoUtil userInfoUtil;

    public ChatDTO getChat(int chatId, int userId) {
        Chat chat = chatRepository.findById(chatId).
                orElseThrow(() -> new ChatNotFoundException("Chat with " + chatId + " not found"));
        validateChatAccess(chat, userId);
        return mapperDTO.toChatDTO(chat);
    }

    public List<ChatDTO> getUserChats(int userId) {
        List<Chat> chats = chatRepository.findByMembersUserId(userId);
        return chats.stream().map(chat ->
                mapperDTO.toChatDTO(chat)).collect(Collectors.toList());
    }

    @Transactional
    public ChatDTO createGroupChat(GroupChatCreationDTO groupChatCreationDTO, int creatorId) {
        Chat chat = mapperDTO.toChat(groupChatCreationDTO, creatorId);

        ChatSettings settings = mapperDTO.toChatSettings();
        settings.setChat(chat);
        chat.setSettings(settings);

        ChatMember chatMember = mapperDTO.toChatMember(chat, creatorId, ChatRole.CREATOR);

        chat = chatRepository.save(chat);

        return mapperDTO.toChatDTO(chat);
    }

    @Transactional
    public ChatDTO createPrivateChat(int senderId, int receiverId){
        if (!userInfoUtil.ifUserExists(receiverId).block()) {
            throw new UserNotFoundException("User " + receiverId + " not found");
        }

        Optional<Chat> existingChat = chatRepository.findPrivateChat(senderId, receiverId);
        if (existingChat.isPresent()) {
            return mapperDTO.toChatDTO(existingChat.get());
        }

        Chat chat = new Chat();
        chat.setType(ChatType.PRIVATE);
        chat.setCreatedBy(senderId);

        List<ChatMember> chatMembers = new ArrayList<>();
        chatMembers.add(mapperDTO.toChatMember(chat, senderId, null));
        chatMembers.add(mapperDTO.toChatMember(chat, receiverId, null));
        chat.setMembers(chatMembers);

        chat = chatRepository.save(chat);

        return mapperDTO.toChatDTO(chat);
    }

    //TODO сделать метод удаления чата, добавить проверку на ismember, isadmin, iscreator
    //TODO добавить методы работы с аватаром, назначение на должности, обновления групп чата

    private void validateChatAccess(Chat chat, int userId) {
        boolean isMember = chat.getMembers().stream()
                .anyMatch(member -> member.getUserId() == userId);
        if (!isMember) {
            throw new AccessDeniedException("User does not have access to this chat");
        }
    }

}
