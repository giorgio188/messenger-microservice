package com.messenger.chat_service.controllers;

import com.messenger.chat_service.dto.FileDTO;
import com.messenger.chat_service.services.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/api/chat/{chatId}/files")
@RestController
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @GetMapping
    public ResponseEntity<Page<FileDTO>> getAllFiles(@RequestHeader("X-User-Id") int userId,
                                                     @PathVariable int chatId,
                                                     @RequestParam(defaultValue = "0") int page,
                                                     @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<FileDTO> files = fileService.getFilesByChatId(userId, chatId, pageRequest);

        return ResponseEntity.ok(files);
    }

    @GetMapping("/images")
    public ResponseEntity<Page<FileDTO>> getImages (@RequestHeader("X-User-Id") int userId,
                                                    @PathVariable int chatId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<FileDTO> images = fileService.getImagesByChatId(userId, chatId, pageRequest);
        return ResponseEntity.ok(images);
    }

    @GetMapping("/documents")
    public ResponseEntity<Page<FileDTO>> getDocuments (
            @RequestHeader("X-User-Id") int userId,
            @PathVariable int chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size);
        Page<FileDTO> documents = fileService.getDocumentsByChatId(userId, chatId, pageRequest);
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<Void> deleteFile(
            @RequestHeader("X-User-Id") int userId,
            @PathVariable int chatId,
            @PathVariable int fileId) {

        fileService.deleteFile(userId, fileId);

        return ResponseEntity.noContent().build();
    }

}
