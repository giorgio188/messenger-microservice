package com.messenger.group_chat_service.services;


import com.messenger.group_chat_service.dto.GroupChatMessageDTO;
import com.messenger.group_chat_service.models.GroupChat;
import com.messenger.group_chat_service.models.GroupChatMessage;
import com.messenger.group_chat_service.models.enums.MessageStatus;
import com.messenger.group_chat_service.repositories.GroupChatMessageRepository;
import com.messenger.group_chat_service.repositories.GroupChatRepository;
import com.messenger.group_chat_service.utils.MapperForDTO;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class GroupChatMessageService {

    private final GroupChatService groupChatService;
    private final EncryptionService encryptionService;
    private final GroupChatMessageRepository groupchatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final MapperForDTO mapperForDTO;
    private final GroupChatRepository groupChatRepository;
    private final ModelMapper modelMapper;


    @Transactional
    public GroupChatMessageDTO sendMessage(int senderId, int groupChatId, String message) {
        GroupChat groupChat = modelMapper.map(groupChatService.getGroupChat(groupChatId, senderId), GroupChat.class);
        String encryptedMessage = encryptionService.encrypt(message);
        GroupChatMessage groupChatMessage = new GroupChatMessage(
                groupChat,
                senderId,
                encryptedMessage,
                MessageStatus.SENT
        );

        GroupChatMessage savedMessage = groupchatMessageRepository.save(groupChatMessage);
        GroupChatMessageDTO messageDTO = mapperForDTO.convertGroupChatMessageToDTO(savedMessage);
        messageDTO.setMessage(message);

        messagingTemplate.convertAndSend(
                "/topic/group-message." + groupChatId,
                messageDTO
        );

        return messageDTO;
    }

    public List<GroupChatMessageDTO> getGroupChatMessages(int groupChatId) {

        List<GroupChatMessage> messages = groupchatMessageRepository
                .findByGroupChatOrderBySentAtDesc(groupChatRepository.findById(groupChatId).get());
        List<GroupChatMessageDTO> messagesDTO = new ArrayList<>();
        for (GroupChatMessage message : messages) {
            String decryptedMessageContent = encryptionService.decrypt(message.getMessage());
            message.setMessage(decryptedMessageContent);
            if (message.getStatus() == MessageStatus.SENT) {
                message.setStatus(MessageStatus.READ);
                groupchatMessageRepository.save(message);
            }
            messagesDTO.add(mapperForDTO.convertGroupChatMessageToDTO(message));
        }

        return messagesDTO;
    }

    @Transactional
    public void deleteGroupMessage(int messageId) {
        Optional<GroupChatMessage> messageOptional = groupchatMessageRepository.findById(messageId);
        if (messageOptional.isPresent()) {
            GroupChatMessage message = messageOptional.get();
            int chatId = message.getGroupChat().getId();
            groupchatMessageRepository.deleteById(messageId);
            Map<String, Object> deleteNotification = new HashMap<>();
            deleteNotification.put("type", "MESSAGE_DELETED");
            deleteNotification.put("messageId", messageId);
            deleteNotification.put("chatId", chatId);
            deleteNotification.put("timestamp", LocalDateTime.now());

            // Отправляем уведомление всем участникам группового чата
            messagingTemplate.convertAndSend(
                    "/topic/group-message." + chatId,
                    deleteNotification
            );

        } else {
            throw new EntityNotFoundException("Message with messageId " + messageId + " not found");
        }
    }

    @Transactional
    public GroupChatMessageDTO editGroupMessage(int messageId, String editedMessage) {
        Optional<GroupChatMessage> groupChatMessage = groupchatMessageRepository.findById(messageId);
        if (groupChatMessage.isPresent()) {

            GroupChatMessage message = groupChatMessage.get();
            int chatId = message.getGroupChat().getId();
            String encryptedEditedMessage = encryptionService.encrypt(editedMessage);
            message.setMessage(encryptedEditedMessage);
            message.setStatus(MessageStatus.EDITED);
            groupchatMessageRepository.save(message);

            GroupChatMessageDTO messageDTO = mapperForDTO.convertGroupChatMessageToDTO(message);
            messageDTO.setMessage(editedMessage);
            Map<String, Object> editNotification = new HashMap<>();
            editNotification.put("type", "MESSAGE_EDITED");
            editNotification.put("messageId", messageId);
            editNotification.put("newMessage", editedMessage);
            editNotification.put("status", MessageStatus.EDITED);
            editNotification.put("chatId", chatId);
            editNotification.put("timestamp", LocalDateTime.now());
            editNotification.put("senderId", message.getSenderId());
            // Отправляем уведомление всем участникам группового чата
            messagingTemplate.convertAndSend(
                    "/topic/group-message." + chatId,
                    editNotification
            );
            return messageDTO;
        }
        else {
            throw new EntityNotFoundException("Message not found with messageId: " + messageId);
        }
    }

    @Transactional
    public void markMessagesAsRead(int chatId, int readerId) {
        GroupChat chat = groupChatRepository.findById(chatId)
                .orElseThrow(() -> new EntityNotFoundException("Chat not found"));

        // Получаем все непрочитанные сообщения, где читающий не является отправителем
        List<GroupChatMessage> unreadMessages = groupchatMessageRepository
                .findByGroupChatAndStatusAndSenderIdNot(
                        chat,
                        MessageStatus.SENT,
                        readerId
                );

        if (!unreadMessages.isEmpty()) {
            // Обновляем статус всех непрочитанных сообщений
            unreadMessages.forEach(message -> message.setStatus(MessageStatus.READ));
            List<GroupChatMessage> updatedMessages = groupchatMessageRepository.saveAll(unreadMessages);

            // Создаем уведомление о прочтении сообщений
            Map<String, Object> readNotification = new HashMap<>();
            readNotification.put("type", "MESSAGES_READ");
            readNotification.put("chatId", chatId);
            readNotification.put("readerId", readerId);
            readNotification.put("timestamp", LocalDateTime.now());
            readNotification.put("messageIds", updatedMessages.stream()
                    .map(GroupChatMessage::getId)
                    .toList());

            // Отправляем уведомление всем участникам группового чата
            messagingTemplate.convertAndSend(
                    "/topic/group-message/" + chatId + "/read",
                    readNotification
            );
        }
    }
}
