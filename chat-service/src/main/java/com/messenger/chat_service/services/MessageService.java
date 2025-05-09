package com.messenger.chat_service.services;

import com.messenger.chat_service.dto.MessageDTO;
import com.messenger.chat_service.exceptions.ChatAccessDeniedException;
import com.messenger.chat_service.exceptions.ChatNotFoundException;
import com.messenger.chat_service.models.Chat;
import com.messenger.chat_service.models.File;
import com.messenger.chat_service.models.Message;
import com.messenger.chat_service.models.enums.ChatRole;
import com.messenger.chat_service.models.enums.ChatType;
import com.messenger.chat_service.models.enums.MessageStatus;
import com.messenger.chat_service.repositories.ChatRepository;
import com.messenger.chat_service.repositories.MessageRepository;
import com.messenger.chat_service.utils.MapperDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final ChatRepository chatRepository;
    private final MapperDTO mapperDTO;
    private final EncryptionService encryptionService;
    private final ChatService chatService;
    private final FileService fileService;
    private final SimpMessagingTemplate messagingTemplate;

    public Page<MessageDTO> getChatMessages(int chatId, int userId, Pageable pageable) {
        chatService.checkChatAccess(chatId, userId);
        Page<Message> messages = messageRepository.findByChatIdWithFilesOrderBySentAtDesc(chatId, pageable);
        messageRepository.markMessagesAsRead(chatId, userId);
        return messages.map(mapperDTO::toMessageDTO);
    }

    @Transactional
    public MessageDTO sendMessage(int chatId, int senderId, String content, List<MultipartFile> files) {
        chatService.checkChatAccess(chatId, senderId);

        Chat chat = chatRepository.findById(chatId)
                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));

        if (chat.getType() == ChatType.GROUP) {
            if (chat.getSettings() != null && chat.getSettings().isOnlyAdminsCanWrite()) {
                boolean isAdmin = chat.getMembers().stream()
                        .filter(member -> member.getUserId() == senderId)
                        .anyMatch(member -> member.getRole() == ChatRole.ADMIN ||
                                member.getRole() == ChatRole.CREATOR);

                if (!isAdmin) {
                    throw new ChatAccessDeniedException("Only admins can write in this chat");
                }
            }

            boolean isMuted = chat.getMembers().stream()
                    .filter(member -> member.getUserId() == senderId)
                    .anyMatch(member -> member.isMuted());
            if (isMuted) {
                throw new ChatAccessDeniedException("You are muted in this chat");
            }
        }

        boolean hasContent = content != null && !content.isEmpty();
        boolean hasFiles = files != null && !files.isEmpty();

        if (!hasContent && !hasFiles) {
            throw new IllegalArgumentException("Content and files cannot be empty");
        }

        Message message = new Message();
        message.setChat(chat);
        message.setSenderId(senderId);

        if (hasContent) {
            message.setContent(encryptionService.encrypt(content));
        } else {
            message.setContent("");
        }

        message.setStatus(MessageStatus.SENT);
        message.setEdited(false);
        message.setDeleted(false);

        message = messageRepository.save(message);

        if (hasFiles) {
            fileService.uploadFilesToMessage(senderId, chatId, files, message);
        }

        message = messageRepository.findById(message.getId())
                .orElseThrow(() -> new RuntimeException("Message not found"));

        MessageDTO messageDTO = mapperDTO.toMessageDTO(message);
        messagingTemplate.convertAndSend("/topic/chat." + chatId, messageDTO);

        return messageDTO;
    }

    @Transactional
    public MessageDTO editMessage(int messageId, int userId, String newContent) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));
        if (message.getSenderId() != userId) {
            throw new ChatAccessDeniedException("You can edit only your own message");
        }

        if (message.isDeleted()) {
            throw new ChatAccessDeniedException("You cannot edit a deleted message");
        }

        message.setContent(encryptionService.encrypt(newContent));
        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());

        message = messageRepository.save(message);
        MessageDTO messageDTO = mapperDTO.toMessageDTO(message);
        messagingTemplate.convertAndSend("/topic/chat." + message.getChat().getId(), messageDTO);
        return messageDTO;
    }

    @Transactional
    public void deleteMessage(int messageId, int userId) {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (message.getSenderId() != userId) {
            Chat chat = message.getChat();
            boolean isAdmin = chat.getMembers().stream()
                    .filter(member -> member.getUserId() == userId)
                    .anyMatch(member -> member.getRole().name().contains("ADMIN")
                    || member.getRole().name().equals("CREATOR"));
            if (!isAdmin) {
                throw new ChatAccessDeniedException("You don't have permission to delete this message");
            }
        }

        message.setDeleted(true);
        messageRepository.save(message);

        MessageDTO messageDTO = mapperDTO.toMessageDTO(message);
        messageDTO.setDeleted(true);
        messageDTO.setContent("Deleted");

        messagingTemplate.convertAndSend("/topic/chat." + message.getChat().getId(), messageDTO);
    }

    public List<MessageDTO> getLastMessages(int chatId, int userId) {
        chatService.checkChatAccess(chatId, userId);

        Pageable pageable = Pageable.ofSize(10);
        List<Message> messages = messageRepository.findTop10ByChatIdWithFilesOrderBySentAtDesc(chatId);

        return messages.stream()
                .map(mapperDTO::toMessageDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void markMessagesAsRead(int chatId, int userId) {
        chatService.checkChatAccess(chatId, userId);
        messageRepository.markMessagesAsRead(chatId, userId);
        messagingTemplate.convertAndSend("/topic/chat." + chatId + ".read", userId);
    }

    public int getUnreadMessagesCount(int chatId, int userId) {
        chatService.checkChatAccess(chatId, userId);
        return messageRepository.countUnreadMessages(chatId, userId);
    }
}

//    @Transactional
//    public MessageDTO sendTextMessage(int chatId, int senderId, String content) {
//        chatService.checkChatAccess(chatId, senderId);
//        Chat chat = chatRepository.findById(chatId)
//                .orElseThrow(() -> new ChatNotFoundException("Chat not found"));
//        if (chat.getType() == ChatType.GROUP) {
//            if (chat.getSettings() != null && chat.getSettings().isOnlyAdminsCanWrite()) {
//                boolean isAdmin = chat.getMembers().stream()
//                        .filter(member -> member.getUserId() == senderId)
//                        .anyMatch(member -> member.getRole() == ChatRole.ADMIN ||
//                                member.getRole() == ChatRole.CREATOR);
//
//                if (!isAdmin) {
//                    throw new ChatAccessDeniedException("Only admins can write in this chat");
//                }
//            }
//
//            boolean isMuted = chat.getMembers().stream()
//                    .filter(member -> member.getUserId() == senderId)
//                    .anyMatch(member -> member.isMuted());
//
//            if (isMuted) {
//                throw new ChatAccessDeniedException("You are muted in this chat");
//            }
//        }
//
//        Message message = new Message();
//        message.setChatId(chat);
//        message.setSenderId(senderId);
//        message.setContent(encryptionService.encrypt(content));
//        message.setStatus(MessageStatus.SENT);
//        message.setEdited(false);
//        message.setDeleted(false);
//        message = messageRepository.save(message);
//
//        MessageDTO messageDTO = mapperDTO.toMessageDTO(message);
//
//        messagingTemplate.convertAndSend("/topic/chat." + chatId, messageDTO);
//
//        return messageDTO;
//    }