package com.messenger.private_chat_service.services;


import com.messenger.private_chat_service.dto.PrivateChatDTO;
import com.messenger.private_chat_service.dto.UserProfileDTO;
import com.messenger.private_chat_service.models.PrivateChat;
import com.messenger.private_chat_service.repositories.PrivateChatRepository;
import com.messenger.private_chat_service.utils.MapperForDTO;
import com.messenger.private_chat_service.utils.UserInfoUtil;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateChatService {

    private final PrivateChatRepository privateChatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MapperForDTO mapperForDTO;
    private final UserInfoUtil userInfoUtil;


    public PrivateChatDTO getPrivateChatBySenderAndReceiver(int senderId, int receiverId) {
        if (userInfoUtil.ifUserExists(senderId) && userInfoUtil.ifUserExists(receiverId)) {
            PrivateChat privateChat = privateChatRepository.findPrivateChatBySenderAndReceiver(senderId, receiverId);
            if (privateChat == null) {
                PrivateChat privateChat2 = privateChatRepository.findPrivateChatBySenderAndReceiver(receiverId, senderId);
                if (privateChat2 == null) {
                    return null;
                }
                return mapperForDTO.convertPrivateChatToDto(privateChat2);
            }
            return mapperForDTO.convertPrivateChatToDto(privateChat);
        } else {
            throw new EntityNotFoundException("One of the users doesn't exist");
        }
    }

    public PrivateChatDTO getPrivateChat(int chatId, int senderId) throws AccessDeniedException {
        PrivateChat privateChat = privateChatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Private chat not found"));

        if (privateChat.getSenderId() != senderId &&
                privateChat.getReceiverId() != senderId) {
            throw new AccessDeniedException("User is not a participant of this chat");
        }
        return mapperForDTO.convertPrivateChatToDto(privateChat);
    }

    public int getChatId(int senderId, int receiverId) {
        PrivateChat privateChat = privateChatRepository.findPrivateChatBySenderAndReceiver(senderId, receiverId);
        if (privateChat == null) {
            privateChat = privateChatRepository.findPrivateChatBySenderAndReceiver(receiverId, senderId);
        }
        if (privateChat == null) {
            throw new EntityNotFoundException("Private chat not found");
        }
        return privateChat.getId();
    }

    public List<UserProfileDTO> getPrivateChatParticipants(int chatId) {
        Optional<PrivateChat> privateChat = privateChatRepository.findPrivateChatById(chatId);
        if (privateChat.isPresent()) {
            UserProfileDTO sender = userInfoUtil.getUserProfile(privateChat.get().getSenderId());
            UserProfileDTO receiver = userInfoUtil.getUserProfile(privateChat.get().getReceiverId());
            List<UserProfileDTO> participants = new ArrayList<>();
            participants.add(sender);
            participants.add(receiver);
            return participants;
        } else throw new EntityNotFoundException("Private chat not found");
    }

    public List<PrivateChatDTO> getAllPrivateChatsOfUser(int userId) {
        if (userInfoUtil.ifUserExists(userId)) {
            List<PrivateChat> allChatsAsSender = privateChatRepository.findPrivateChatBySender(userId);
            List<PrivateChat> allChatsAsReceiver = privateChatRepository.findPrivateChatByReceiver(userId);
            List<PrivateChat> allChats = new ArrayList<>();
            allChats.addAll(allChatsAsSender);
            allChats.addAll(allChatsAsReceiver);
            List<PrivateChatDTO> allChatsDTO = new ArrayList<>();
            for (PrivateChat privateChat : allChats) {
                allChatsDTO.add(mapperForDTO.convertPrivateChatToDto(privateChat));
            }
            return allChatsDTO;
        }      else {
            throw new EntityNotFoundException("User not found");
        }
    }

    @Transactional
    public void createPrivateChat(int senderId, int receiverId) {
        PrivateChat privateChat = new PrivateChat(senderId, receiverId);
        PrivateChat savedChat = privateChatRepository.save(privateChat);
        messagingTemplate.convertAndSend(
                "/topic/chats/" + senderId,
                mapperForDTO.convertPrivateChatToDto(savedChat)
        );

        messagingTemplate.convertAndSend(
                "/topic/chats/" + receiverId,
                mapperForDTO.convertPrivateChatToDto(savedChat)
        );
    }

    @Transactional
    public void deletePrivateChat(int id) {
        privateChatRepository.deleteById(id);
    }

}
