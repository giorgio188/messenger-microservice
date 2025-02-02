package com.messenger.private_chat_service.controllers;


import com.messenger.private_chat_service.models.PrivateChatFiles;
import com.messenger.private_chat_service.services.PrivateChatFileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/private-file")
@CrossOrigin(origins = "http://localhost:3000")
public class PrivateChatFileController {

    private final PrivateChatFileService privateChatFileService;
    private final JWTUtil jwtUtil;

    @GetMapping("/{privateChatId}")
    public ResponseEntity<List<PrivateChatFiles>> getPrivateChatFiles(@PathVariable int privateChatId) {
        List<PrivateChatFiles> privateChatFiles = privateChatFileService.getPrivateChatFiles(privateChatId);
        return ResponseEntity.ok(privateChatFiles);
    }

    @PostMapping("/{privateChatId}")
    public ResponseEntity<PrivateChatFiles> sendPrivateChatFile(@RequestHeader("Authorization") String token,
                                                                @PathVariable int privateChatId,
                                                                @RequestParam MultipartFile file) {
        int senderId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        PrivateChatFiles privateChatFile = privateChatFileService.sendPrivateChatFile(senderId, privateChatId, file);
        return ResponseEntity.ok(privateChatFile);
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deletePrivateChatFile(@PathVariable int fileId) {
        privateChatFileService.deletePrivateChatFile(fileId);
        return ResponseEntity.ok("File deleted");
    }

}
//    // WebSocket endpoint для отправки файла
//    @MessageMapping("/private.file.upload")
//    public void handlePrivateFileUpload(@Payload MultipartFileWrapper payload,
//                                        SimpMessageHeaderAccessor headerAccessor) {
//        String token = headerAccessor.getFirstNativeHeader("Authorization");
//        if (token != null && token.startsWith("Bearer ")) {
//            int senderId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
//            try {
//                privateChatFileService.sendPrivateChatFile(senderId, payload.getChatId(), payload.getFile());
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to upload file: " + e.getMessage());
//            }
//        }
//    }
//
//    // WebSocket endpoint для удаления файла
//    @MessageMapping("/private.file.delete")
//    public void handlePrivateFileDelete(@Payload Map<String, Object> payload,
//                                        SimpMessageHeaderAccessor headerAccessor) {
//        String token = headerAccessor.getFirstNativeHeader("Authorization");
//        if (token != null && token.startsWith("Bearer ")) {
//            int fileId = (Integer) payload.get("fileId");
//            try {
//                privateChatFileService.deletePrivateChatFile(fileId);
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to delete file: " + e.getMessage());
//            }
//        }
//    }

