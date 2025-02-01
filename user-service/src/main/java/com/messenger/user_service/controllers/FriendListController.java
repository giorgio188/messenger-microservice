package com.messenger.user_service.controllers;


import com.messenger.user_service.dto.FriendDTO;
import com.messenger.user_service.models.UserProfile;
import com.messenger.user_service.services.FriendListService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class FriendListController {

    private final FriendListService friendListService;

    @GetMapping("/{userId}")
    public ResponseEntity<List<FriendDTO>> getFriendList(@PathVariable int userId) {
        List<FriendDTO> friendList = friendListService.getFriendList(userId);
        return ResponseEntity.ok(friendList);
    }

    // WebSocket endpoint для добавления друга
    @MessageMapping("/friend.add")
    public void handleAddFriend(@Payload Map<String, Object> payload,
                                SimpMessageHeaderAccessor headerAccessor) {
        String userIdStr = headerAccessor.getFirstNativeHeader("X-User-Id");
        if (userIdStr != null) {
            try {
                int userId = Integer.parseInt(userIdStr);
                int friendId = (Integer) payload.get("friendId");
                friendListService.addFriend(userId, friendId);
            } catch (NumberFormatException e) {
                Logger logger = LoggerFactory.getLogger(this.getClass());
                logger.error("Failed to parse X-User-Id '{}' as an integer", userIdStr, e);
            } catch (Exception e) {
                throw new RuntimeException("Failed to add friend: " + e.getMessage());
            }
        }
    }

    @MessageMapping("/friend.delete")
    public void handleDeleteFriend(@Payload Map<String, Object> payload,
                                   SimpMessageHeaderAccessor headerAccessor) {
        String userIdStr = headerAccessor.getFirstNativeHeader("X-User-Id");
        if (userIdStr != null) {
            try {
                int userId = Integer.parseInt(userIdStr);
                int friendId = (Integer) payload.get("friendId");
                friendListService.deleteFriend(userId, friendId);
            } catch (NumberFormatException e) {
                Logger logger = LoggerFactory.getLogger(this.getClass());
                logger.error("Failed to parse X-User-Id '{}' as an integer", userIdStr, e);
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete friend: " + e.getMessage());
            }
        }
    }

}

//    @PostMapping("/addFriend")
//    @ResponseStatus(HttpStatus.FOUND)
//    public ResponseEntity<?> addFriend (@RequestHeader("Authorization") String token,
//                             @RequestParam int friendId) {
//        int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
//        friendListService.addFriend(userId, friendId);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
//
//    @DeleteMapping("/deleteFriend")
//    @ResponseStatus(HttpStatus.FOUND)
//    public ResponseEntity<?> deleteFriend (@RequestHeader("Authorization") String token,
//                                @RequestParam int friendId) {
//        int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
//        friendListService.deleteFriend(userId, friendId);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
