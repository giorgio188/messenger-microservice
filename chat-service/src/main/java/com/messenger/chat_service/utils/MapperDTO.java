package com.messenger.chat_service.utils;

import com.messenger.chat_service.dto.*;
import com.messenger.chat_service.models.*;
import com.messenger.chat_service.models.enums.ChatRole;
import com.messenger.chat_service.models.enums.ChatType;
import com.messenger.chat_service.services.EncryptionService;
import com.messenger.chat_service.services.S3Service;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class MapperDTO {

    private final ModelMapper modelMapper;
    private final S3Service s3Service;
    private final UserInfoUtil userInfoUtil;
    private final EncryptionService encryptionService;

    public ChatDTO toChatDTO(Chat chat) {
        ChatDTO dto = new ChatDTO();
        dto.setId(chat.getId());
        dto.setType(chat.getType());
        dto.setName(chat.getName());
        dto.setDescription(chat.getDescription());
        dto.setAvatar(chat.getAvatar());
        dto.setCreatedBy(chat.getCreatedBy());
        dto.setCreatedAt(chat.getCreatedAt());
        dto.setUpdatedAt(chat.getUpdatedAt());

        if (chat.getSettings() != null) {
            dto.setSettings(toChatSettingsDTO(chat.getSettings()));
        }

        List<ChatMemberDTO> members = chat.getMembers().stream()
                .map(this::toChatMemberDTO)
                .collect(Collectors.toList());
        dto.setMembers(members);

        return dto;
    }

    public ChatMemberDTO toChatMemberDTO(ChatMember member) {
        ChatParticipantDTO userProfile = userInfoUtil.getUserProfile(member.getUserId())
                .block(Duration.ofSeconds(5));

        ChatMemberDTO dto = new ChatMemberDTO();
        dto.setUserId(member.getUserId());
        dto.setUsername(userProfile.getUsername());
        dto.setNickname(userProfile.getNickname());
        dto.setAvatar(userProfile.getAvatar());
        dto.setRole(member.getRole());
        dto.setJoinedAt(member.getJoinedAt());
        dto.setLastReadAt(member.getLastReadAt());
        return dto;
    }

    public ChatSettingsDTO toChatSettingsDTO(ChatSettings settings) {
        ChatSettingsDTO dto = new ChatSettingsDTO();
        dto.setOnlyAdminsCanWrite(settings.isOnlyAdminsCanWrite());
        dto.setOnlyAdminsCanAddMembers(settings.isOnlyAdminsCanAddMembers());
        dto.setOnlyAdminsCanRemoveMembers(settings.isOnlyAdminsCanRemoveMembers());
        dto.setOnlyAdminsCanChangeInfo(settings.isOnlyAdminsCanChangeInfo());
        return dto;
    }

    public Chat toChat(GroupChatCreationDTO request, int creatorId) {
        Chat chat = new Chat();
        chat.setType(ChatType.GROUP);
        chat.setName(request.getName());
        chat.setDescription(request.getDescription());
        chat.setCreatedBy(creatorId);
        return chat;
    }

    public ChatMember toChatMember(Chat chat, int userId, ChatRole role) {
        ChatMember member = new ChatMember();
        member.setChat(chat);
        member.setUserId(userId);
        member.setRole(role);
        member.setMuted(false);
        member.setLastReadAt(LocalDateTime.now());
        return member;
    }

    public ChatSettings toChatSettings() {
        ChatSettings settings = new ChatSettings();
        settings.setOnlyAdminsCanWrite(false);
        settings.setOnlyAdminsCanAddMembers(false);
        settings.setOnlyAdminsCanRemoveMembers(false);
        settings.setOnlyAdminsCanChangeInfo(false);
        return settings;
    }

    public MessageDTO toMessageDTO(Message message) {
        MessageDTO dto = modelMapper.map(message, MessageDTO.class);

        if (message.getContent() != null && !message.getContent().isEmpty()) {
            dto.setContent(encryptionService.decrypt(message.getContent()));
        }

        try {
            ChatParticipantDTO sender = userInfoUtil.getUserProfile(message.getSenderId()).block();
            if (sender != null) {
                dto.setSenderUsername(sender.getUsername());
                dto.setSenderNickname(sender.getNickname());
                dto.setSenderAvatar(sender.getAvatar());
            }
        } catch (Exception e) {
            dto.setSenderUsername("Unknown user");
        }

        if (message.getFiles() !=null && !message.getFiles().isEmpty()) {
            List<FileDTO> files = message.getFiles().stream()
                    .filter(file -> !file.isDeleted())
                    .map(file -> modelMapper.map(file, FileDTO.class))
                    .collect(Collectors.toList());
            dto.setFiles(files);
        } else {
            dto.setFiles(null);
        }

        return dto;
    }

    public FileDTO toFileDTO(File file) {

        FileDTO dto = modelMapper.map(file, FileDTO.class);

        dto.setFileUrl(s3Service.getFileUrl(file.getFilePath()));

        try {
            ChatParticipantDTO sender = userInfoUtil.getUserProfile(file.getSenderId()).block();
            if (sender != null) {
                dto.setSenderUsername(sender.getUsername());
                dto.setSenderNickname(sender.getNickname());
            }
        } catch (Exception e) {
            dto.setSenderUsername("Unknown user");
        }
        if (file.getMessage() != null) {
            dto.setMessageId(file.getMessage().getId());
        }

        return dto;
    }
}
