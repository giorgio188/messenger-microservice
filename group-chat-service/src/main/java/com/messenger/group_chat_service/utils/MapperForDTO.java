package com.messenger.group_chat_service.utils;


import com.messenger.group_chat_service.dto.GroupChatDTO;
import com.messenger.group_chat_service.dto.GroupChatMembersDTO;
import com.messenger.group_chat_service.dto.GroupChatMessageDTO;
import com.messenger.group_chat_service.models.GroupChat;
import com.messenger.group_chat_service.models.GroupChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class MapperForDTO {


    private final UserInfoUtil userInfoUtil;

    public GroupChatMessageDTO convertGroupChatMessageToDTO(GroupChatMessage groupChatMessage) {
        GroupChatMessageDTO dto = new GroupChatMessageDTO();
        dto.setId(groupChatMessage.getId());
        dto.setGroupChatId(groupChatMessage.getGroupChat().getId());
        int senderId = groupChatMessage.getSenderId();
        dto.setSenderId(senderId);
        dto.setSenderUsername(userInfoUtil.getUserProfile(senderId).getUsername());
        dto.setSenderNickname(userInfoUtil.getUserProfile(senderId).getNickname());
        dto.setMessage(groupChatMessage.getMessage());
        dto.setSendTime(groupChatMessage.getSentAt());
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
            int memberId = member.getId();
            memberDTO.setMemberId(memberId);
            memberDTO.setUsername(userInfoUtil.getUserProfile(memberId).getUsername());
            memberDTO.setNickname(userInfoUtil.getUserProfile(memberId).getNickname());
            memberDTO.setRole(member.getRole().toString());
            return memberDTO;
        }).collect(Collectors.toList());
        groupChatDTO.setMembers(membersDTO);
        return groupChatDTO;
    }

}
