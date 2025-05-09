package com.messenger.chat_service.controllers;

import com.messenger.chat_service.dto.MessageDTO;
import com.messenger.chat_service.services.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@RequestMapping("/api/chat/{chatId}/messages")
@RestController
@RequiredArgsConstructor
public class MessageRestController {

    private final MessageService messageService;

    @GetMapping
    public ResponseEntity<Page<MessageDTO>> getMessages(
            @RequestHeader("X-User-Id") int userId,
            @PathVariable int chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "sentAt"));
        Page<MessageDTO> messages = messageService.getChatMessages(chatId, userId, pageRequest);

        return ResponseEntity.ok(messages);
    }


    @GetMapping("/last")
    public ResponseEntity<List<MessageDTO>> getLastMessages(
            @RequestHeader("X-User-Id") int userId,
            @PathVariable int chatId) {

        List<MessageDTO> messages = messageService.getLastMessages(chatId, userId);
        return ResponseEntity.ok(messages);
    }


    @GetMapping("/unread/count")
    public ResponseEntity<Integer> getUnreadMessagesCount(
            @RequestHeader("X-User-Id") int userId,
            @PathVariable int chatId) {

        int count = messageService.getUnreadMessagesCount(chatId, userId);

        return ResponseEntity.ok(count);
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MessageDTO> uploadFilesAndSendMessage(
            @RequestHeader("X-User-Id") int userId,
            @PathVariable int chatId,
            @RequestParam(required = false) String content,
            @RequestParam(value = "files", required = false) List<MultipartFile> files) {

        if (files == null) {
            files = Collections.emptyList();
        }

        MessageDTO message = messageService.sendMessage(chatId, userId, content, files);

        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

}
