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

        dto.setSenderId(privateChatMessage.getSenderId());
        dto.setSenderUsername(userInfoUtil.getUserProfile(privateChatMessage.getSenderId()).getUsername());
        dto.setSenderNickname(userInfoUtil.getUserProfile(privateChatMessage.getSenderId()).getNickname());

        dto.setReceiverId(privateChatMessage.getReceiverId());
        dto.setReceiverUsername(userInfoUtil.getUserProfile(privateChatMessage.getReceiverId()).getUsername());
        dto.setReceiverNickname(userInfoUtil.getUserProfile(privateChatMessage.getReceiverId()).getNickname());

        dto.setMessage(privateChatMessage.getMessage());
        dto.setSendTime(privateChatMessage.getSentAt());
        return dto;
    }

    public PrivateChatDTO convertPrivateChatToDto(PrivateChat chat) {
        PrivateChatDTO dto = new PrivateChatDTO();
        dto.setId(chat.getId());

        dto.setSenderId(chat.getSenderId());
        dto.setSenderUsername(userInfoUtil.getUserProfile(chat.getSenderId()).getUsername());
        dto.setSenderNickname(userInfoUtil.getUserProfile(chat.getSenderId()).getNickname());

        dto.setReceiverId(chat.getReceiverId());
        dto.setReceiverUsername(userInfoUtil.getUserProfile(chat.getReceiverId()).getUsername());
        dto.setReceiverNickname(userInfoUtil.getUserProfile(chat.getReceiverId()).getNickname());

        dto.setCreatedAt(chat.getCreatedAt());
        return dto;
    }

}
