package com.messenger.chat_service.controllers;

import com.messenger.chat_service.dto.MessageDTO;
import com.messenger.chat_service.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/api/chat")
@RestController
@RequiredArgsConstructor
public class MessageWebsocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/{chatId}/send")
    public void sendMessage(@DestinationVariable int chatId,
                            @Payload Map<String, String> payload,
                            @Header("X-User-Id") int userId,
                            SimpMessageHeaderAccessor headerAccessor) {
        String content = payload.get("content");

        try {
            MessageDTO message = messageService.sendMessage(chatId, userId, content, null);
        } catch (Exception e) {
            sendErrorToUser(userId, "Error sending message: " + e.getMessage());
        }
    }

    @MessageMapping("/{chatId}/edit/{messageId}")
    public void editMessage(@DestinationVariable int chatId,
                            @DestinationVariable int messageId,
                            @Payload Map<String, String> payload,
                            @Header("X-User-Id") int userId) {
        String content = payload.get("content");

        try {
            MessageDTO message = messageService.editMessage(messageId, userId, content);
        } catch (Exception e) {
            sendErrorToUser(userId, "Error editing message: " + e.getMessage());
        }
    }


    @MessageMapping("/{chatId}/delete/{messageId}")
    public void deleteMessage(@DestinationVariable int chatId,
                              @DestinationVariable int messageId,
                              @Header("X-User-Id") int userId) {
        try {
            messageService.deleteMessage(messageId, userId);
        } catch (Exception e) {
            sendErrorToUser(userId, "Error deleting message: " + e.getMessage());
        }
    }

    @MessageMapping("/{chatId}/mark-read")
    public void markMessagesAsRead(@DestinationVariable int chatId,
                                   @Header("X-User-Id") int userId) {
        try {
            messageService.markMessagesAsRead(chatId, userId);
        } catch (Exception e) {
            sendErrorToUser(userId, "Error marking messages as read: " + e.getMessage());
        }
    }

    @MessageMapping("/chat/{chatId}/typing/start")
    public void handleTypingStart(
            @DestinationVariable int chatId,
            SimpMessageHeaderAccessor headerAccessor) {

        String userIdStr = headerAccessor.getFirstNativeHeader("X-User-Id");
        String deviceId = headerAccessor.getFirstNativeHeader("X-Device-Id");

        if (userIdStr != null) {
            try {
                int userId = Integer.parseInt(userIdStr);
                messagingTemplate.convertAndSend(
                        "/topic/chat." + chatId + ".typing",
                        Map.of(
                                "userId", userId,
                                "eventType", "TYPING_START",
                                "timestamp", System.currentTimeMillis()
                        )
                );
            } catch (NumberFormatException e) {
                return;
            }
        }
    }

    @MessageMapping("/chat/{chatId}/typing/stop")
    public void handleTypingStop(
            @DestinationVariable int chatId,
            SimpMessageHeaderAccessor headerAccessor) {

        String userIdStr = headerAccessor.getFirstNativeHeader("X-User-Id");

        if (userIdStr != null) {
            try {
                int userId = Integer.parseInt(userIdStr);
                messagingTemplate.convertAndSend(
                        "/topic/chat." + chatId + ".typing",
                        Map.of(
                                "userId", userId,
                                "eventType", "TYPING_STOP",
                                "timestamp", System.currentTimeMillis()
                        )
                );
            } catch (NumberFormatException e) {
                return;
            }
        }
    }

    private void sendErrorToUser(int userId, String errorMessage) {
        messagingTemplate.convertAndSendToUser(
                String.valueOf(userId),
                "/queue/errors",
                Map.of("error", errorMessage)
        );
    }

}
