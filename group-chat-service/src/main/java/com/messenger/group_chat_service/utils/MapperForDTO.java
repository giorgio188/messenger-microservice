package com.messenger.group_chat_service.utils;

import com.project.messenger.dto.*;
import com.project.messenger.models.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class MapperForDTO {

    public PrivateChatMessageDTO convertPrivateMessageToDTO(PrivateChatMessage privateChatMessage) {
        PrivateChatMessageDTO dto = new PrivateChatMessageDTO();
        dto.setId(privateChatMessage.getId());
        dto.setPrivateChatId(privateChatMessage.getPrivateChat().getId());
        dto.setSenderId(privateChatMessage.getSender().getId());
        dto.setSenderUsername(privateChatMessage.getSender().getUsername());
        dto.setSenderNickname(privateChatMessage.getSender().getNickname());
        dto.setReceiverUsername(privateChatMessage.getReceiver().getUsername());
        dto.setReceiverNickname(privateChatMessage.getReceiver().getNickname());
        dto.setMessage(privateChatMessage.getMessage());
        dto.setSendTime(privateChatMessage.getSentAt());
        return dto;
    }

    public GroupChatMessageDTO convertGroupChatMessageToDTO(GroupChatMessage groupChatMessage) {
        GroupChatMessageDTO dto = new GroupChatMessageDTO();
        dto.setId(groupChatMessage.getId());
        dto.setGroupChatId(groupChatMessage.getGroupChat().getId());
        dto.setSenderId(groupChatMessage.getSender().getId());
        dto.setSenderUsername(groupChatMessage.getSender().getUsername());
        dto.setSenderNickname(groupChatMessage.getSender().getNickname());
        dto.setMessage(groupChatMessage.getMessage());
        dto.setSendTime(groupChatMessage.getSentAt());
        return dto;
    }

    public PrivateChatDTO convertPrivateChatToDto(PrivateChat chat) {
        PrivateChatDTO dto = new PrivateChatDTO();
        dto.setId(chat.getId());
        dto.setSenderId(chat.getSender().getId());
        dto.setSenderUsername(chat.getSender().getUsername());
        dto.setSenderNickname(chat.getSender().getNickname());
        dto.setReceiverId(chat.getReceiver().getId());
        dto.setReceiverUsername(chat.getReceiver().getUsername());
        dto.setReceiverNickname(chat.getReceiver().getNickname());
        dto.setCreatedAt(chat.getCreatedAt());
        return dto;
    }

    public GroupChatDTO convertGroupChatToDTO(GroupChat groupChat) {
        GroupChatDTO groupChatDTO = new GroupChatDTO();
        groupChatDTO.setId(groupChat.getId());
        groupChatDTO.setName(groupChat.getName());
        groupChatDTO.setDescription(groupChat.getDescription());
        groupChatDTO.setCreatedAt(LocalDateTime.now());
        List<GroupChatMembersDTO> membersDTO = groupChat.getGroupChatMembers().stream().map(member -> {
            GroupChatMembersDTO memberDTO = new GroupChatMembersDTO();
            memberDTO.setMemberId(String.valueOf(member.getMember().getId()));
            memberDTO.setUsername(member.getMember().getUsername());
            memberDTO.setNickname(member.getMember().getNickname());
            memberDTO.setRole(member.getRole().toString());
            return memberDTO;
        }).collect(Collectors.toList());
        groupChatDTO.setMembers(membersDTO);
        return groupChatDTO;
    }

    public UserUtilDTO convertUserToDTO(UserProfile userProfile) {
        UserUtilDTO dto = new UserUtilDTO();
        dto.setId(userProfile.getId());
        dto.setUsername(userProfile.getUsername());
        dto.setNickname(userProfile.getNickname());
        return dto;
    }

    public UserProfilePageDTO convertUserToProfilePageDTO(UserProfile userProfile) {
        UserProfilePageDTO dto = new UserProfilePageDTO();
        dto.setUsername(userProfile.getUsername());
        dto.setNickname(userProfile.getNickname());
        dto.setEmail(userProfile.getEmail());
        dto.setPhoneNumber(userProfile.getPhoneNumber());
        dto.setAvatar(userProfile.getAvatar());
        return dto;
    }

}
