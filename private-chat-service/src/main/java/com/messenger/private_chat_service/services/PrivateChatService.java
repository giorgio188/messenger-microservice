package com.messenger.private_chat_service.services;


import com.messenger.private_chat_service.dto.PrivateChatDTO;
import com.messenger.private_chat_service.dto.UserUtilDTO;
import com.messenger.private_chat_service.models.PrivateChat;
import com.messenger.private_chat_service.repositories.PrivateChatRepository;
import com.messenger.private_chat_service.utils.MapperForDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateChatService {

    private final UserProfileRepository userProfileRepository;
    private final PrivateChatRepository privateChatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MapperForDTO mapperForDTO;


    public PrivateChatDTO getPrivateChatBySenderAndReceiver(int senderId, int receiverId) {
        UserProfile sender = userProfileRepository.findById(senderId).orElse(null);
        UserProfile receiver = userProfileRepository.findById(receiverId).orElse(null);

        PrivateChat privateChat = privateChatRepository.findPrivateChatBySenderAndReceiver(sender, receiver);
        if (privateChat == null) {
            PrivateChat privateChat2 = privateChatRepository.findPrivateChatBySenderAndReceiver(receiver, sender);
            if (privateChat2 == null) {
                return null;
            }
            return mapperForDTO.convertPrivateChatToDto(privateChat2);
        }
        return mapperForDTO.convertPrivateChatToDto(privateChat);
    }

    public PrivateChatDTO getPrivateChat(int chatId, int senderId) throws AccessDeniedException {
        PrivateChat privateChat = privateChatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Private chat not found"));

        if (privateChat.getSender().getId() != senderId &&
                privateChat.getReceiver().getId() != senderId) {
            throw new AccessDeniedException("User is not a participant of this chat");
        }
        return mapperForDTO.convertPrivateChatToDto(privateChat);
    }

    // Метод для получения ID чата по участникам
    public int getChatId(int senderId, int receiverId) {
        UserProfile sender = userProfileRepository.findById(senderId)
                .orElseThrow(() -> new EntityNotFoundException("Sender not found"));
        UserProfile receiver = userProfileRepository.findById(receiverId)
                .orElseThrow(() -> new EntityNotFoundException("Receiver not found"));

        PrivateChat privateChat = privateChatRepository.findPrivateChatBySenderAndReceiver(sender, receiver);
        if (privateChat == null) {
            privateChat = privateChatRepository.findPrivateChatBySenderAndReceiver(receiver, sender);
        }
        if (privateChat == null) {
            throw new EntityNotFoundException("Private chat not found");
        }
        return privateChat.getId();
    }

    public List<UserUtilDTO> getPrivateChatParticipants(int chatId) {
        Optional<PrivateChat> privateChat = privateChatRepository.findPrivateChatById(chatId);
        if (privateChat.isPresent()) {
            UserUtilDTO sender = mapperForDTO.convertUserToDTO(privateChat.get().getSender());
            UserUtilDTO receiver = mapperForDTO.convertUserToDTO(privateChat.get().getReceiver());
            List<UserUtilDTO> participants = new ArrayList<>();
            participants.add(sender);
            participants.add(receiver);
            return participants;
        } else throw new EntityNotFoundException("Private chat not found");
    }

    public List<PrivateChatDTO> getAllChatsOfOneUser(int id) {
        Optional<UserProfile> userProfile = userProfileRepository.findById(id);
        List<PrivateChat> allChatsAsSender = privateChatRepository.findPrivateChatBySender(userProfile.orElse(null));
        List<PrivateChat> allChatsAsReceiver = privateChatRepository.findPrivateChatByReceiver(userProfile.orElse(null));
        List<PrivateChat> allChats = new ArrayList<>();
        allChats.addAll(allChatsAsSender);
        allChats.addAll(allChatsAsReceiver);
        List<PrivateChatDTO> allChatsDTO = new ArrayList<>();
        for (PrivateChat privateChat : allChats) {
            allChatsDTO.add(mapperForDTO.convertPrivateChatToDto(privateChat));
        }
        return allChatsDTO;
    }

    @Transactional
    public void createPrivateChat(int senderId, int receiverId) {
        UserProfile sender = userProfileRepository.findById(senderId).orElse(null);
        UserProfile receiver = userProfileRepository.findById(receiverId).orElse(null);
        PrivateChat privateChat = new PrivateChat(sender, receiver, LocalDateTime.now());
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
