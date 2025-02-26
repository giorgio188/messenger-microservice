package com.messenger.chat_service.controllers;

import com.messenger.chat_service.dto.ChatDTO;
import com.messenger.chat_service.dto.GroupChatCreationDTO;
import com.messenger.chat_service.services.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequestMapping("/api/chat")
@RestController
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/{chatId}")
    public ResponseEntity<ChatDTO> getChat(@RequestHeader("X-User-Id") int userId,
                                           @PathVariable int chatId) {
        ChatDTO chatDTO = chatService.getChat(userId, chatId);
        return ResponseEntity.ok(chatDTO);
    }

    @GetMapping
    public ResponseEntity<List<ChatDTO>> getAllChats(@RequestHeader("X-User-Id") int userId) {
        List<ChatDTO> chats = chatService.getUserChats(userId);
        return ResponseEntity.ok(chats);
    }

    @PostMapping("/group")
    public ResponseEntity<ChatDTO> createGroupChat(@RequestHeader("X-User-Id") int userId,
            @RequestBody GroupChatCreationDTO groupChatCreationDTO) {
        ChatDTO chat = chatService.createGroupChat(groupChatCreationDTO, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(chat);
    }

    @PostMapping("/private/{receiverId}")
    public ResponseEntity<ChatDTO> createPrivateChat(@RequestHeader("X-User-Id") int userId,
                                                     @PathVariable int receiverId) {
        ChatDTO chat = chatService.createPrivateChat(receiverId, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(chat);
    }

    @PatchMapping("/{chatId}")
    public ResponseEntity<ChatDTO> updateChat(@RequestHeader("X-User-Id") int userId,
                                              @PathVariable int chatId,
                                              @RequestBody GroupChatCreationDTO GroupChatCreationDTO) {
        ChatDTO chat = chatService.updateChat(userId, chatId, GroupChatCreationDTO);
        return ResponseEntity.ok(chat);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteChat(@RequestHeader("X-User-Id") int userId,
            @PathVariable int chatId) {
        chatService.deleteChat(userId, chatId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{chatId}/avatar")
    public ResponseEntity<String> uploadAvatar(@RequestHeader("X-User-Id") int userId,
                                               @PathVariable int chatId,
                                               MultipartFile file) {
        String avatar = chatService.uploadAvatar(userId, chatId, file);
        return ResponseEntity.ok(avatar);
    }

    @DeleteMapping("/{chatId}/avatar")
    public ResponseEntity<Void> deleteAvatar(@RequestHeader("X-User-Id") int userId,
                                             @PathVariable int chatId) {
        chatService.deleteChatAvatar(userId, chatId);
        return ResponseEntity.noContent().build();
    }

}
