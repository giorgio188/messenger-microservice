package com.messenger.private_chat_service.controllers;


import com.messenger.private_chat_service.dto.PrivateChatDTO;
import com.messenger.private_chat_service.dto.UserProfileDTO;
import com.messenger.private_chat_service.services.PrivateChatMessageService;
import com.messenger.private_chat_service.services.PrivateChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/private-chat")
@CrossOrigin(origins = "http://localhost:3000")
public class PrivateChatController {

    private final PrivateChatService privateChatService;

    private final PrivateChatMessageService privateChatMessageService;

    @GetMapping("/{privateChatId}")
    public ResponseEntity<PrivateChatDTO> getPrivateChat(
            @RequestHeader("X-User-Id") int userId,
            @PathVariable int privateChatId) throws AccessDeniedException {
        PrivateChatDTO privateChat = privateChatService.getPrivateChat(privateChatId, userId);
        return ResponseEntity.ok(privateChat);
    }

    @GetMapping("/find/{receiverId}")
    public ResponseEntity<PrivateChatDTO> getPrivateChatBySenderAndReceiver(
            @RequestHeader("X-User-Id") int userId,
            @PathVariable int receiverId) {
        try {
            PrivateChatDTO privateChat = privateChatService.getPrivateChatBySenderAndReceiver(userId, receiverId);
            if (privateChat == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
            }
            return ResponseEntity.ok(privateChat);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping()
    public ResponseEntity<List<PrivateChatDTO>> getPrivateChatsOfUser(@RequestHeader("X-User-Id") int userId) {
        List<PrivateChatDTO> chats = privateChatService.getAllPrivateChatsOfUser(userId);
        return ResponseEntity.ok(chats);
    }

    @GetMapping("/{privateChatId}/members")
    public ResponseEntity<List<UserProfileDTO>> getPrivateChatMembers(@PathVariable int privateChatId) {
        List<UserProfileDTO> privateChatMembers = privateChatService.getPrivateChatParticipants(privateChatId);
        return ResponseEntity.ok(privateChatMembers);
    }

    @PostMapping("/create/{receiverId}")
    public ResponseEntity<PrivateChatDTO> createPrivateChat(@RequestHeader("X-User-Id") int userId,
                                                            @PathVariable int receiverId) {
        privateChatService.createPrivateChat(userId, receiverId);
        PrivateChatDTO privateChat = privateChatService.getPrivateChatBySenderAndReceiver(userId, receiverId);
        return ResponseEntity.ok(privateChat);
    }

    @DeleteMapping("/{privateChatId}")
    public ResponseEntity<HttpStatus> deletePrivateChat(@PathVariable int privateChatId) {
        privateChatService.deletePrivateChat(privateChatId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    // WebSocket endpoint для входа пользователя в чат
    @MessageMapping("/private.enter")
    public void handleChatEnter(@Payload Map<String, Object> payload,
                                SimpMessageHeaderAccessor headerAccessor) {
        String userIdStr = headerAccessor.getFirstNativeHeader("X-User-Id");
        if (userIdStr != null) {
            int userId = Integer.parseInt(userIdStr);
            int privateChatId = (Integer) payload.get("privateChatId");
            try {
                privateChatMessageService.markMessagesAsRead(privateChatId, userId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to mark messages as read: " + e.getMessage());
            }
        }
    }
}
