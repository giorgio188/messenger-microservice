package com.messenger.group_chat_service.controllers;

import com.project.messenger.dto.GroupChatCreatingDTO;
import com.project.messenger.dto.GroupChatDTO;
import com.project.messenger.models.GroupChat;
import com.project.messenger.models.GroupChatMembers;
import com.project.messenger.models.enums.Roles;
import com.project.messenger.security.JWTUtil;
import com.project.messenger.services.GroupChatMessageService;
import com.project.messenger.services.GroupChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RequestMapping("api/group-chat")
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class GroupChatController {


    private final GroupChatService groupChatService;
    private final JWTUtil jwtUtil;
    private final GroupChatMessageService groupChatMessageService;

    @GetMapping("/{groupChatId}")
    public ResponseEntity<GroupChatDTO> getGroupChat(
            @RequestHeader("Authorization") String token,
            @PathVariable int groupChatId){
        int memberId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        GroupChatDTO groupChat = groupChatService.getGroupChat(groupChatId, memberId);
        return  ResponseEntity.ok(groupChat);
    }

    @GetMapping()
    public ResponseEntity<List<GroupChatDTO>> getGroupChatsByMember(
            @RequestHeader("Authorization") String token) {
        int memberId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        List<GroupChatDTO> groupChats = groupChatService.getAllGroupChatsByUser(memberId);
        return  ResponseEntity.ok(groupChats);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGroupChat(
            @RequestHeader("Authorization") String token,
            @RequestBody GroupChatCreatingDTO groupChatDTO) {
        int creatorId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        groupChatService.createGroupChat(
                groupChatDTO.getName(),
                groupChatDTO.getDescription(),
                creatorId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupChatId}")
    public ResponseEntity<HttpStatus> deleteGroupChat(@RequestHeader("Authorization") String token,
                                  @PathVariable int groupChatId) {
        groupChatService.deleteGroupChat(groupChatId, jwtUtil.extractUserId(token.replace("Bearer ", "")));
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PatchMapping("/{groupChatId}/edit-description")
    public ResponseEntity<GroupChat> editGroupChatDescription(
            @RequestHeader("Authorization") String token,
            @PathVariable int groupChatId,
            @RequestParam String newDesc) {
        groupChatService.editDescription(groupChatId, newDesc, jwtUtil.extractUserId(token.replace("Bearer ", "")));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{groupChatId}/edit-name")
    public ResponseEntity<GroupChat> editGroupChatName(
            @RequestHeader("Authorization") String token,
            @PathVariable int groupChatId,
            @RequestParam String newName) {
        groupChatService.editName(groupChatId, newName, jwtUtil.extractUserId(token.replace("Bearer ", "")));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupChatId}/delete-user")
    public ResponseEntity<GroupChat> deleteUser(
            @RequestHeader("Authorization") String token,
            @PathVariable int groupChatId,
            @RequestParam int userId) {
        groupChatService.deleteUser(groupChatId, userId, jwtUtil.extractUserId(token.replace("Bearer ", "")));
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{groupChatId}/add-user")
    public ResponseEntity<GroupChat> addUser(
            @PathVariable int groupChatId,
            @RequestParam int userId) {
        groupChatService.addUser(groupChatId, userId, Roles.MEMBER);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{groupChatId}/change-role/{memberId}")
    public ResponseEntity<?> changeRole(@RequestHeader("Authorization") String token,
                                                       @PathVariable int groupChatId,
                                                       @PathVariable int memberId,
                                                       @RequestParam Roles role) {
        groupChatService.setRoleToMember(groupChatId, memberId, role, jwtUtil.extractUserId(token.replace("Bearer ", "")));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupChatId}/members")
    public ResponseEntity<List<GroupChatMembers>> getGroupChatMembers(@PathVariable int groupChatId) {
        List<GroupChatMembers>  members =  groupChatService.getAllGroupChatMembersByGroupChat(groupChatId);
        return ResponseEntity.ok(members);
    }

    @DeleteMapping("/{groupChatId}/leave")
    public ResponseEntity<GroupChat> leaveGroupChatByUser(
            @RequestHeader("Authorization") String token,
            @PathVariable int groupChatId) {
        int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        groupChatService.leaveGroupChat(groupChatId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupChatId}/avatar")
    public ResponseEntity<String> getGroupChatAvatar(@PathVariable int groupChatId) {
        return ResponseEntity.ok(groupChatService.getGroupChatAvatar(groupChatId));
    }

    @PatchMapping("/{groupChatId}/avatar")
    public ResponseEntity<?> uploadGroupChatAvatar(@RequestHeader("Authorization") String token,
                                                   @PathVariable int groupChatId,
                                                   @RequestParam MultipartFile file) {
        try {
            int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            groupChatService.setAvatar(userId, groupChatId, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupChatId}/avatar")
    public void deleteGroupChatAvatar(@RequestHeader("Authorization") String token,
                                                   @PathVariable int groupChatId) {
        groupChatService.deleteAvatar(groupChatId, jwtUtil.extractUserId(token.replace("Bearer ", "")));
    }

    @MessageMapping("/group.enter")
    public void handleChatEnter(@Payload Map<String, Object> payload,
                                SimpMessageHeaderAccessor headerAccessor) {
        String token = headerAccessor.getFirstNativeHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            int groupChatId = (Integer) payload.get("groupChatId");

            try {
                groupChatMessageService.markMessagesAsRead(groupChatId, userId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to mark messages as read: " + e.getMessage());
            }
        }
    }



}
