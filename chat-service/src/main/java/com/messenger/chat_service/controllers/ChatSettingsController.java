package com.messenger.chat_service.controllers;

import com.messenger.chat_service.dto.ChatSettingsDTO;
import com.messenger.chat_service.services.ChatSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/chat/{chatId}/settings")
@RestController
@RequiredArgsConstructor
public class ChatSettingsController {
    private final ChatSettingsService chatSettingsService;

    @GetMapping
    public ResponseEntity<ChatSettingsDTO> getChatSettings(@RequestHeader("X-User-Id") int userId,
                                                           @PathVariable int chatId) {
        ChatSettingsDTO chatSettingsDTO = chatSettingsService.getChatSettings(userId, chatId);
        return ResponseEntity.ok(chatSettingsDTO);
    }

    @PatchMapping
    public ResponseEntity<Void> updateChatSettings(@RequestHeader("X-User-Id") int userId,
                                                              @PathVariable int chatID,
                                                              @RequestBody ChatSettingsDTO chatSettingsDTO) {
        chatSettingsService.updateChatSettings(userId, chatID, chatSettingsDTO);
        return ResponseEntity.ok().build();
    }
}
