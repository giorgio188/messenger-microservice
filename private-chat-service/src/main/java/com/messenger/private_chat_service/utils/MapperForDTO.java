package com.messenger.private_chat_service.utils;


import com.messenger.private_chat_service.dto.PrivateChatDTO;
import com.messenger.private_chat_service.dto.PrivateChatMessageDTO;
import com.messenger.private_chat_service.models.PrivateChat;
import com.messenger.private_chat_service.models.PrivateChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Autowired})
public class MapperForDTO {

    private final UserInfoUtil userInfoUtil;

    public PrivateChatMessageDTO convertPrivateMessageToDTO(PrivateChatMessage privateChatMessage) {
        PrivateChatMessageDTO dto = new PrivateChatMessageDTO();
        dto.setId(privateChatMessage.getId());
        dto.setPrivateChatId(privateChatMessage.getPrivateChat().getId());

        int senderId = privateChatMessage.getSenderId();
        dto.setSenderId(senderId);
        dto.setSenderUsername(userInfoUtil.getUserProfile(senderId).getUsername());
        dto.setSenderNickname(userInfoUtil.getUserProfile(senderId).getNickname());

        int receiverId = privateChatMessage.getReceiverId();
        dto.setReceiverId(receiverId);
        dto.setReceiverUsername(userInfoUtil.getUserProfile(receiverId).getUsername());
        dto.setReceiverNickname(userInfoUtil.getUserProfile(receiverId).getNickname());

        dto.setMessage(privateChatMessage.getMessage());
        dto.setSendTime(privateChatMessage.getSentAt());
        return dto;
    }

    public PrivateChatDTO convertPrivateChatToDto(PrivateChat chat) {
        PrivateChatDTO dto = new PrivateChatDTO();
        dto.setId(chat.getId());

        int senderId = chat.getSenderId();
        dto.setSenderId(senderId);
        dto.setSenderUsername(userInfoUtil.getUserProfile(senderId).getUsername());
        dto.setSenderNickname(userInfoUtil.getUserProfile(senderId).getNickname());

        int receiverId = chat.getReceiverId();
        dto.setReceiverId(receiverId);
        dto.setReceiverUsername(userInfoUtil.getUserProfile(receiverId).getUsername());
        dto.setReceiverNickname(userInfoUtil.getUserProfile(receiverId).getNickname());

        dto.setCreatedAt(chat.getCreatedAt());
        return dto;
    }

}
