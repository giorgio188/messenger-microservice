package com.messenger.chat_service.services;

import com.messenger.chat_service.dto.ChatDTO;
import com.messenger.chat_service.dto.GroupChatCreationDTO;
import com.messenger.chat_service.exceptions.ChatAccessDeniedException;
import com.messenger.chat_service.exceptions.ChatNotFoundException;
import com.messenger.chat_service.exceptions.ChatValidationException;
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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
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
    public ChatDTO createPrivateChat(int senderId, int receiverId) {
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

    @Transactional
    public ChatDTO updateChat(int chatId, int userId, GroupChatCreationDTO dto) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found with id: " + chatId));

        if (chat.getType() != ChatType.GROUP) {
            throw new ChatValidationException("Only group chats can be updated");
        }

        if (chat.getSettings().isOnlyAdminsCanChangeInfo()) {
            boolean isAdmin = chat.getMembers().stream()
                    .filter(member -> member.getUserId() == userId)
                    .anyMatch(member -> member.getRole() == ChatRole.ADMIN || member.getRole() == ChatRole.CREATOR);
            if (!isAdmin) {
                throw new ChatAccessDeniedException("Only admins can update chat details");
            }
        } else {
            boolean isMember = chat.getMembers().stream()
                    .anyMatch(member -> member.getUserId() == userId);
            if (!isMember) {
                throw new ChatAccessDeniedException("User is not a member of this chat");
            }
        }

        if (dto.getName() != null && !dto.getName().isEmpty()) {
            chat.setName(dto.getName());
        }

        if (dto.getDescription() != null && !dto.getDescription().isEmpty()) {
            chat.setDescription(dto.getDescription());
        }

        chat = chatRepository.save(chat);
        return mapperDTO.toChatDTO(chat);
    }

    @Transactional
    public void deleteChat(int chatId, int userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found with id: " + chatId));

        if (chat.getType() == ChatType.GROUP) {
            boolean isCreator = chat.getMembers().stream()
                    .filter(member -> member.getUserId() == userId)
                    .anyMatch(member -> member.getRole() == ChatRole.CREATOR);

            if (!isCreator) {
                throw new ChatAccessDeniedException("Only the creator can delete this chat");
            }
        } else {
            boolean isMember = chat.getMembers().stream()
                    .anyMatch(member -> member.getUserId() == userId);

            if (!isMember) {
                throw new ChatAccessDeniedException("User is not a member of this chat");
            }
        }

        chatRepository.delete(chat);
    }

    @Transactional
    public String uploadAvatar(int chatId, int userId, MultipartFile file) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found with id: " + chatId));
        if (chat.getType() == ChatType.GROUP) {
            boolean isAdmin = chat.getMembers().stream()
                    .filter(member -> member.getUserId() == userId)
                    .anyMatch(member -> member.getRole() == ChatRole.ADMIN || member.getRole() == ChatRole.CREATOR);

            if (!isAdmin) {
                throw new ChatAccessDeniedException("Only admins can update chat avatar");
            }
        }

        try {
            String avatarUrl = s3Service.uploadFile(file, "groupchat-avatars");
            if (chat.getAvatar() != null && !chat.getAvatar().isEmpty()) {
                s3Service.deleteFile(chat.getAvatar());
            }
            chat.setAvatar(avatarUrl);
            chatRepository.save(chat);
            return avatarUrl;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload file to S3: " + e.getMessage());
        }

    }

    @Transactional
    public void deleteChatAvatar(int chatId, int userId) {
        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found with id: " + chatId));

        if (chat.getType() == ChatType.GROUP) {
            boolean isAdmin = chat.getMembers().stream()
                    .filter(member -> member.getUserId() == userId)
                    .anyMatch(member -> member.getRole() == ChatRole.ADMIN || member.getRole() == ChatRole.CREATOR);

            if (!isAdmin) {
                throw new ChatAccessDeniedException("Only admins can update chat avatar");
            }
        } else {
            throw new ChatValidationException("Private chats do not have avatars");
        }

        if (chat.getAvatar() != null && !chat.getAvatar().isEmpty()) {
            try {
                s3Service.deleteFile(chat.getAvatar());

                chat.setAvatar(null);
                chatRepository.save(chat);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete avatar: " + e.getMessage(), e);
            }
        }
    }

    public void checkChatAccess(int chatId, int userId) {
        Chat chat = chatRepository.findById(chatId).orElseThrow(() -> new ChatNotFoundException("Chat not found with id: " + chatId));
        validateChatAccess(chat, userId);
    }

    private void validateChatAccess(Chat chat, int userId) {
        boolean isMember = chat.getMembers().stream()
                .anyMatch(member -> member.getUserId() == userId);
        if (!isMember) {
            throw new AccessDeniedException("User does not have access to this chat");
        }
    }

}
