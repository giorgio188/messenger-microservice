package com.messenger.private_chat_service.controllers;


import com.messenger.private_chat_service.dto.PrivateChatMessageDTO;
import com.messenger.private_chat_service.repositories.PrivateChatMessageRepository;
import com.messenger.private_chat_service.services.PrivateChatMessageService;
import com.messenger.private_chat_service.services.PrivateChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/private-message")
@CrossOrigin(origins = "http://localhost:3000")
public class PrivateChatMessageController {

    private final PrivateChatMessageService privateChatMessageService;
    private final PrivateChatService privateChatService;
    private final PrivateChatMessageRepository privateChatMessageRepository;

    @GetMapping("/{privateChatId}")
    public ResponseEntity<List<PrivateChatMessageDTO>> getPrivateChatMessages(@PathVariable int privateChatId) {
        List<PrivateChatMessageDTO> messages = privateChatMessageService.getPrivateChatMessages(privateChatId);
        return ResponseEntity.ok(messages);
    }

    @MessageMapping("/privateMessage.send")
    public void handlePrivateMessage(@Payload Map<String, Object> payload,
                                     SimpMessageHeaderAccessor headerAccessor) {
        String senderIdStr = headerAccessor.getFirstNativeHeader("X-User-Id");
        if (senderIdStr != null) {
            try {
                int senderId = Integer.parseInt(senderIdStr);
                int chatId = (Integer) payload.get("chatId");
                String message = (String) payload.get("message");
                try {
                    privateChatMessageService.sendMessage(senderId, chatId, message);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to send message: " + e.getMessage());
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to send message: " + e.getMessage());
            }

        }
    }

    @MessageMapping("/privateMessage.delete")
    public void handleDeleteMessage(@Payload Map<String, Object> payload,
                                    SimpMessageHeaderAccessor headerAccessor) {
            String userIdStr = headerAccessor.getFirstNativeHeader("X-User-Id");
            if (userIdStr != null) {
                try {
                    int currentUserId = Integer.parseInt(userIdStr);
                    int messageId = (Integer) payload.get("messageId");
                    int senderId = privateChatMessageService.getPrivateChatMessage(messageId).getSenderId();
                    if (senderId == currentUserId) {
                        try {
                            privateChatMessageService.deletePrivateMessage(messageId);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to delete message: " + e.getMessage());
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException("Failed to delete message: " + e.getMessage());
                }
        }
    }

    @MessageMapping("/privateMessage.edit")
    public void handleEditMessage(@Payload Map<String, Object> payload,
                                  SimpMessageHeaderAccessor headerAccessor) {
        String userIdStr = headerAccessor.getFirstNativeHeader("X-User-Id");
        if (userIdStr != null) {
            try {
                int currentUserId = Integer.parseInt(userIdStr);
                int messageId = (Integer) payload.get("messageId");
                int senderId = privateChatMessageService.getPrivateChatMessage(messageId).getSenderId();
                String editedMessage = (String) payload.get("editedMessage");
                if (senderId == currentUserId) {
                    try {
                        privateChatMessageService.editPrivateMessage(messageId, editedMessage);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to edit message: " + e.getMessage());
                    }

                }
            } catch (Exception e) {}

        }
    }
}



//    @PostMapping("/{privateChatId}")
//    public ResponseEntity<PrivateChatMessageDTO> sendMessage(
//            @RequestHeader("Authorization") String token,
//            @PathVariable int privateChatId,
//            @RequestParam String message) throws AccessDeniedException {
//        int senderId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
//        PrivateChatDTO chat = privateChatService.getPrivateChat(privateChatId, senderId);
//        if (chat == null) {
//            throw new AccessDeniedException("У вас нет доступа к этому чату");
//        }
//        PrivateChatMessageDTO sentMessage = privateChatMessageService.sendMessage(senderId, privateChatId, message);
//        return ResponseEntity.ok(sentMessage);
//    }
//    @DeleteMapping("/{messageId}")
//    public ResponseEntity<String> deletePrivateMessage(@PathVariable int messageId) {
//        privateChatMessageService.deletePrivateMessage(messageId);
//        return ResponseEntity.ok("Message deleted");
//    }
//
//    @PatchMapping("/{messageId}")
//    public ResponseEntity<PrivateChatMessage> editPrivateMessage(
//            @PathVariable int messageId,
//            @RequestParam String editedMessage) {
//        PrivateChatMessage updatedMessage = privateChatMessageService.editPrivateMessage(messageId, editedMessage);
//        return ResponseEntity.ok(updatedMessage);
//    }