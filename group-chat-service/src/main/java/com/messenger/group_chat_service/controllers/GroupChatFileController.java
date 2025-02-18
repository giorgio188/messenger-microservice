package com.messenger.group_chat_service.controllers;


import com.messenger.group_chat_service.models.GroupChat;
import com.messenger.group_chat_service.models.GroupChatFiles;
import com.messenger.group_chat_service.services.GroupChatFileService;
import com.messenger.group_chat_service.services.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

@RequestMapping("api/group-file")
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class GroupChatFileController {

    private final GroupChatService groupChatService;
    private final GroupChatFileService groupChatFileService;
    private final ModelMapper modelMapper;

    @GetMapping("/{groupChatId}")
    public ResponseEntity<List<GroupChatFiles>> getGroupChatFiles(
            @PathVariable int groupChatId) {
        List<GroupChatFiles> files =groupChatFileService.getAllFiles(groupChatId);
        return ResponseEntity.ok(files);
    }

    @PostMapping("/{groupChatId}")
    public ResponseEntity<GroupChatFiles> sendFile(
            @RequestHeader("X-User-Id") int senderId,
            @PathVariable int groupChatId,
            @RequestParam MultipartFile file) throws IOException {
        GroupChat groupChat = modelMapper.map(groupChatService.getGroupChat(groupChatId, senderId), GroupChat.class);
        if (groupChat == null) {
            throw new AccessDeniedException("Access denied");
        }
        GroupChatFiles groupChatFile = groupChatFileService.sendFile(senderId, groupChatId, file);
        return ResponseEntity.ok(groupChatFile);
    }
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(
            @PathVariable int fileId) {
        groupChatFileService.deleteFile(fileId);
        return ResponseEntity.ok("File was deleted");
    }
    //TODO сделать проверку на отправителя файла

}

//    // WebSocket endpoint для отправки файла
//    @MessageMapping("/group.file.upload")
//    public void handleFileUpload(@Payload MultipartFileWrapper payload,
//                                 SimpMessageHeaderAccessor headerAccessor) {
//        String token = headerAccessor.getFirstNativeHeader("Authorization");
//        if (token != null && token.startsWith("Bearer ")) {
//            int senderId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
//            try {
//                groupChatFileService.sendFile(senderId, payload.getChatId(), payload.getFile());
//            } catch (IOException e) {
//                throw new RuntimeException("Failed to upload file: " + e.getMessage());
//            }
//        }
//    }
//
//    // WebSocket endpoint для удаления файла
//    @MessageMapping("/group.file.delete")
//    public void handleFileDelete(@Payload Map<String, Object> payload,
//                                 SimpMessageHeaderAccessor headerAccessor) {
//        String token = headerAccessor.getFirstNativeHeader("Authorization");
//        if (token != null && token.startsWith("Bearer ")) {
//            int fileId = (Integer) payload.get("fileId");
//            try {
//                groupChatFileService.deleteFile(fileId);
//            } catch (Exception e) {
//                throw new RuntimeException("Failed to delete file: " + e.getMessage());
//            }
//        }
//    }