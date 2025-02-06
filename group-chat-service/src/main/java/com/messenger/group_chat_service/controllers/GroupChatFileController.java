package com.messenger.group_chat_service.controllers;

import com.project.messenger.models.GroupChat;
import com.project.messenger.models.GroupChatFiles;
import com.project.messenger.security.JWTUtil;
import com.project.messenger.services.GroupChatFileService;
import com.project.messenger.services.GroupChatService;
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
    private final JWTUtil jwtUtil;
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
            @RequestHeader("Authorization") String token,
            @PathVariable int groupChatId,
            @RequestParam MultipartFile file) throws IOException {
        int memberId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        GroupChat groupChat = modelMapper.map(groupChatService.getGroupChat(groupChatId, memberId), GroupChat.class);
        if (groupChat == null) {
            throw new AccessDeniedException("Access denied");
        }
        GroupChatFiles groupChatFile = groupChatFileService.sendFile(memberId, groupChatId, file);
        return ResponseEntity.ok(groupChatFile);
    }
    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteMessage(
            @PathVariable int fileId) {
        groupChatFileService.deleteFile(fileId);
        return ResponseEntity.ok("File was deleted");
    }

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