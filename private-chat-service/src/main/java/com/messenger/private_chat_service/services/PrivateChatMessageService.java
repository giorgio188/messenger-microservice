package com.messenger.private_chat_service.services;


import com.messenger.private_chat_service.dto.PrivateChatMessageDTO;
import com.messenger.private_chat_service.models.PrivateChat;
import com.messenger.private_chat_service.models.PrivateChatMessage;
import com.messenger.private_chat_service.repositories.PrivateChatMessageRepository;
import com.messenger.private_chat_service.repositories.PrivateChatRepository;
import com.messenger.private_chat_service.utils.MapperForDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrivateChatMessageService {

    private final PrivateChatMessageRepository privateChatMessageRepository;
    private final UserProfileService userProfileService;
    private final EncryptionService encryptionService;
    private final PrivateChatRepository privateChatRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MapperForDTO mapperForDTO;



    @Transactional
    public PrivateChatMessageDTO sendMessage(int senderId, int privateChatId, String message) {
        PrivateChat privateChat = privateChatRepository.findById(privateChatId)
                .orElseThrow(() -> new RuntimeException("Чат не найден"));
        UserProfile sender = userProfileService.getUserProfile(senderId);
        UserProfile receiver = privateChat.getSender().getId() == senderId
                ? privateChat.getReceiver()
                : privateChat.getSender();
        String encryptedMessage = encryptionService.encrypt(message);
        PrivateChatMessage privateChatMessage = new PrivateChatMessage(privateChat, sender, receiver,
                LocalDateTime.now(), encryptedMessage, MessageStatus.SENT);
        privateChatMessageRepository.save(privateChatMessage);
        PrivateChatMessageDTO messageDTO = mapperForDTO.convertPrivateMessageToDTO(privateChatMessage);
        messageDTO.setMessage(message);


        // Отправляем уведомление отправителю для мгновенного обновления его интерфейса
        messagingTemplate.convertAndSendToUser(
                String.valueOf(senderId),
                "/queue/private-messages/" + privateChatId,
                messageDTO
        );

        // Отправляем уведомление получателю
        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiver.getId()),
                "/queue/private-messages/" + privateChatId,
                messageDTO
        );

        messagingTemplate.convertAndSend(
                "/topic/private-message." + privateChatId, // меняем формат топика
                messageDTO
        );

        return mapperForDTO.convertPrivateMessageToDTO(privateChatMessage);
    }

    public List<PrivateChatMessageDTO> getPrivateChatMessages(int privateChatId) {
        List<PrivateChatMessage> messages = privateChatMessageRepository
                .findByPrivateChatOrderBySentAtDesc(privateChatRepository.findById(privateChatId).get());
        List<PrivateChatMessageDTO> messagesDTO = new ArrayList<>();
        for (PrivateChatMessage message : messages) {
            String decryptedMessageContent = encryptionService.decrypt(message.getMessage());
            message.setMessage(decryptedMessageContent);
            PrivateChatMessageDTO messageDTO = mapperForDTO.convertPrivateMessageToDTO(message);
            messagesDTO.add(messageDTO);
        }
        return messagesDTO;
    }

    @Transactional
    public void deletePrivateMessage(int messageId) {
        Optional<PrivateChatMessage> messageOptional = privateChatMessageRepository.findById(messageId);
        if (messageOptional.isPresent()) {
            PrivateChatMessage message = messageOptional.get();
            PrivateChat chat = message.getPrivateChat();
            privateChatMessageRepository.deleteById(messageId);

            Map<String, Object> deleteNotification = new HashMap<>();
            deleteNotification.put("type", "MESSAGE_DELETED");
            deleteNotification.put("messageId", messageId);
            deleteNotification.put("chatId", chat.getId());
            deleteNotification.put("timestamp", LocalDateTime.now());

            // Уведомляем обоих участников чата об удалении сообщения
            messagingTemplate.convertAndSend(
                    "/topic/private-message." + chat.getId(),
                    deleteNotification
            );
        } else {
            throw new EntityNotFoundException("Message with messageId " + messageId + " not found");
        }
    }


    @Transactional
    public PrivateChatMessageDTO editPrivateMessage(int messageId, String editedMessage) {
        Optional<PrivateChatMessage> privateChatMessage = privateChatMessageRepository.findById(messageId);
        if (privateChatMessage.isPresent()) {
            PrivateChatMessage message = privateChatMessage.get();
            PrivateChat chat = message.getPrivateChat();
            String encryptedEditedMessage = encryptionService.encrypt(editedMessage);
            message.setMessage(encryptedEditedMessage);
            message.setStatus(MessageStatus.EDITED);
            privateChatMessageRepository.save(message);

            PrivateChatMessageDTO messageDTO = mapperForDTO.convertPrivateMessageToDTO(message);
            messageDTO.setMessage(editedMessage);

            Map<String, Object> editNotification = new HashMap<>();
            editNotification.put("type", "MESSAGE_EDITED");
            editNotification.put("messageId", messageId);
            editNotification.put("newMessage", editedMessage);
            editNotification.put("chatId", chat.getId());
            editNotification.put("status", MessageStatus.EDITED);
            editNotification.put("timestamp", LocalDateTime.now());
            editNotification.put("senderId", message.getSender().getId());

            // Уведомляем обоих участников чата об изменении сообщения
            messagingTemplate.convertAndSend(
                    "/topic/private-message." + chat.getId(),
                    editNotification
            );
            return messageDTO;
        } else {
            throw new EntityNotFoundException("Message not found with messageId: " + messageId);
        }
    }

    @Transactional
    public void markMessagesAsRead(int chatId, int readerId) {
        PrivateChat chat = privateChatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        // Получаем все непрочитанные сообщения, отправленные другим пользователем
        List<PrivateChatMessage> unreadMessages = privateChatMessageRepository
                .findByPrivateChatAndStatusAndReceiverIdAndSenderIdNot(
                        chat,
                        MessageStatus.SENT,
                        readerId,
                        readerId
                );

        if (!unreadMessages.isEmpty()) {
            // Обновляем статус всех непрочитанных сообщений
            unreadMessages.forEach(message -> {
                message.setStatus(MessageStatus.READ);
            });

            List<PrivateChatMessage> updatedMessages = privateChatMessageRepository.saveAll(unreadMessages);

            // Создаем уведомление о прочтении сообщений
            Map<String, Object> readNotification = new HashMap<>();
            readNotification.put("type", "MESSAGES_READ");
            readNotification.put("chatId", chatId);
            readNotification.put("readerId", readerId);
            readNotification.put("timestamp", LocalDateTime.now());
            readNotification.put("messageIds", updatedMessages.stream()
                    .map(PrivateChatMessage::getId)
                    .toList());

            // Получаем ID другого участника чата
            int otherUserId = (chat.getSender().getId() == readerId)
                    ? chat.getReceiver().getId()
                    : chat.getSender().getId();

            // Отправляем уведомление отправителю сообщений
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(otherUserId),
                    "/queue/private-messages/" + chatId + "/read",
                    readNotification
            );
        }
    }

}
