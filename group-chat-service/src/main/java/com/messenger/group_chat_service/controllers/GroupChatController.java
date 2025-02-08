package com.messenger.group_chat_service.controllers;

import com.messenger.group_chat_service.dto.GroupChatCreatingDTO;
import com.messenger.group_chat_service.dto.GroupChatDTO;
import com.messenger.group_chat_service.models.GroupChat;
import com.messenger.group_chat_service.models.GroupChatMembers;
import com.messenger.group_chat_service.models.enums.Roles;
import com.messenger.group_chat_service.services.GroupChatMessageService;
import com.messenger.group_chat_service.services.GroupChatService;
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
    private final GroupChatMessageService groupChatMessageService;

    @GetMapping("/{groupChatId}")
    public ResponseEntity<GroupChatDTO> getGroupChat(
            @RequestHeader("X-User-Id") int userId,
            @PathVariable int groupChatId){
        GroupChatDTO groupChat = groupChatService.getGroupChat(groupChatId, userId);
        return  ResponseEntity.ok(groupChat);
    }

    @GetMapping()
    public ResponseEntity<List<GroupChatDTO>> getGroupChatsByMember(
            @RequestHeader("X-User-Id") int userId) {
        List<GroupChatDTO> groupChats = groupChatService.getAllGroupChatsByUser(userId);
        return  ResponseEntity.ok(groupChats);
    }

    @PostMapping("/create")
    public ResponseEntity<?> createGroupChat(
            @RequestHeader("X-User-Id") int userId,
            @RequestBody GroupChatCreatingDTO groupChatDTO) {
                groupChatService.createGroupChat(
                groupChatDTO.getName(),
                groupChatDTO.getDescription(),
                userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupChatId}")
    public ResponseEntity<HttpStatus> deleteGroupChat(@RequestHeader("X-User-Id") int userId,
                                  @PathVariable int groupChatId) {
        groupChatService.deleteGroupChat(groupChatId, userId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PatchMapping("/{groupChatId}/edit-description")
    public ResponseEntity<GroupChat> editGroupChatDescription(
            @RequestHeader("X-User-Id") int userId,
            @PathVariable int groupChatId,
            @RequestParam String newDesc) {
        groupChatService.editDescription(groupChatId, newDesc, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{groupChatId}/edit-name")
    public ResponseEntity<GroupChat> editGroupChatName(
            @RequestHeader("X-User-Id") int userId,
            @PathVariable int groupChatId,
            @RequestParam String newName) {
        groupChatService.editName(groupChatId, newName, userId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupChatId}/delete-user")
    public ResponseEntity<GroupChat> deleteUser(
            @RequestHeader("X-User-Id") int userId,
            @PathVariable int groupChatId,
            @RequestParam int userToDeleteId) {
        groupChatService.deleteUser(groupChatId, userToDeleteId, userId);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{groupChatId}/add-user")
    public ResponseEntity<GroupChat> addUser(
            @RequestHeader("X-User-Id") int currentUserId,
            @PathVariable int groupChatId,
            @RequestParam int userToAddId) {
        groupChatService.addUser(groupChatId, currentUserId, userToAddId, Roles.MEMBER);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{groupChatId}/change-role/{memberId}")
    public ResponseEntity<?> changeRole(@RequestHeader("X-User-Id") int userId,
                                                       @PathVariable int groupChatId,
                                                       @PathVariable int memberId,
                                                       @RequestParam Roles role) {
        groupChatService.setRoleToMember(groupChatId, memberId, role, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupChatId}/members")
    public ResponseEntity<List<GroupChatMembers>> getGroupChatMembers(@PathVariable int groupChatId) {
        List<GroupChatMembers>  members =  groupChatService.getAllGroupChatMembersByGroupChat(groupChatId);
        return ResponseEntity.ok(members);
    }

    @DeleteMapping("/{groupChatId}/leave")
    public ResponseEntity<GroupChat> leaveGroupChatByUser(
            @RequestHeader("X-User-Id") int userId,
            @PathVariable int groupChatId) {
        groupChatService.leaveGroupChat(groupChatId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{groupChatId}/avatar")
    public ResponseEntity<String> getGroupChatAvatar(@PathVariable int groupChatId) {
        return ResponseEntity.ok(groupChatService.getGroupChatAvatar(groupChatId));
    }

    @PatchMapping("/{groupChatId}/avatar")
    public ResponseEntity<?> uploadGroupChatAvatar(@RequestHeader("X-User-Id") int userId,
                                                   @PathVariable int groupChatId,
                                                   @RequestParam MultipartFile file) {
        try {
            groupChatService.setAvatar(userId, groupChatId, file);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{groupChatId}/avatar")
    public void deleteGroupChatAvatar(@RequestHeader("X-User-Id") int userId,
                                                   @PathVariable int groupChatId) {
        groupChatService.deleteAvatar(groupChatId, userId);
    }

    @MessageMapping("/group.enter")
    public void handleChatEnter(@Payload Map<String, Object> payload,
                                SimpMessageHeaderAccessor headerAccessor) {
        String userIdStr = headerAccessor.getFirstNativeHeader("X-User-Id");
        if (userIdStr != null) {
            int userId = Integer.parseInt(userIdStr);
            int groupChatId = (Integer) payload.get("groupChatId");

            try {
                groupChatMessageService.markMessagesAsRead(groupChatId, userId);
            } catch (Exception e) {
                throw new RuntimeException("Failed to mark messages as read: " + e.getMessage());
            }
        }
    }



}
