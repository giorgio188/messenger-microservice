package com.messenger.user_service.controllers;


import com.messenger.user_service.dto.UserProfileDTO;
import com.messenger.user_service.dto.UserProfilePageDTO;
import com.messenger.user_service.models.UserProfile;
import com.messenger.user_service.models.enums.ProfileStatus;
import com.messenger.user_service.repositories.UserProfileRepository;
import com.messenger.user_service.services.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/user")
@CrossOrigin(origins = "http://localhost:3000")
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final ModelMapper modelMapper;
    private final UserProfileRepository userProfileRepository;


    @GetMapping("/{userId}")
    public ResponseEntity<UserProfilePageDTO> getUserProfile(@PathVariable int userId) {
        UserProfile userProfile = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(modelMapper.map(userProfile, UserProfilePageDTO.class));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getCurrentUserProfile(@RequestHeader("X-User-Id") int userId) {
        UserProfile userProfile = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    @PatchMapping("/update")
    public ResponseEntity<UserProfile> updateUserProfile(@RequestBody UserProfileDTO userProfileDTO,
                                                         @RequestHeader("X-User-Id") int userId) {
        UserProfile updatedUserProfile = modelMapper.map(userProfileDTO, UserProfile.class);
        userProfileService.updateUserProfile(userId, updatedUserProfile);
        return ResponseEntity.ok(updatedUserProfile);
    }

    @PostMapping("/updatePassword")
    public void updateUserProfilePassword(@RequestHeader("X-User-Id") int userId,
                                          @RequestParam String newPassword) {
        userProfileService.changePassword(userId, newPassword);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity<HttpStatus> deleteUserProfile(@RequestHeader("X-User-Id") int userId) {
        userProfileService.deleteUserProfile(userId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/search") public ResponseEntity<List<UserProfile>> searchUsers(@RequestParam String query) {
        List<UserProfile> searchResults = userProfileService.searchUsers(query);
        return ResponseEntity.ok(searchResults);
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> addUserAvatar(@RequestHeader("X-User-Id") int userId,
                                                     @RequestParam MultipartFile file) {
        try {
            String getExtension = file.getContentType();
            if (getExtension == null || !getExtension.startsWith("image/")) {
                return ResponseEntity.badRequest().body("You can upload only image as avatar");
            }
            String avatarURL = userProfileService.uploadAvatar(userId, file);
            return ResponseEntity.ok(avatarURL);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not upload avatar" + e.getMessage());
        }
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<?> deleteUserAvatar(@RequestHeader("X-User-Id") int userId) {
        userProfileService.deleteAvatar(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/avatar")
    public ResponseEntity<String> getUserAvatar(@RequestHeader("X-User-Id") int userId) {
        String avatarFileName = userProfileRepository.findById(userId).get().getAvatar();
        String avatarLink = userProfileService.getAvatarLink(avatarFileName);
        return ResponseEntity.ok(avatarLink);
    }

    @GetMapping("/avatar/{userId}")
    public ResponseEntity<String> getAnyUserAvatarLink(@PathVariable int userId) {
        String avatarFileName = userProfileRepository.findById(userId).get().getAvatar();
        String avatarLink = userProfileService.getAvatarLink(avatarFileName);
        return ResponseEntity.ok(avatarLink);
    }

    @MessageMapping("/user.connect")
    public void handleUserConnect(SimpMessageHeaderAccessor headerAccessor) {
        String userIdStr = headerAccessor.getFirstNativeHeader("X-User-Id");
        if (userIdStr != null) {
            try {
                int userId = Integer.parseInt(userIdStr);
                userProfileService.setUserOnlineStatus(userId, ProfileStatus.ONLINE);
            } catch (NumberFormatException e) {
                Logger logger = LoggerFactory.getLogger(this.getClass());
                logger.error("Failed to parse X-User-Id '{}' as an integer", userIdStr, e);
            }
        }
    }

    @MessageMapping("/user.disconnect")
    public void handleUserDisconnect(SimpMessageHeaderAccessor headerAccessor) {
        String userIdStr = headerAccessor.getFirstNativeHeader("X-User-Id");
        if (userIdStr != null) {
            try {
                int userId = Integer.parseInt(userIdStr);
                userProfileService.setUserOnlineStatus(userId, ProfileStatus.OFFLINE);
            } catch (NumberFormatException e) {
                Logger logger = LoggerFactory.getLogger(this.getClass());
                logger.error("Failed to parse X-User-Id '{}' as an integer", userIdStr, e);
            }
        }
    }


}
//    @GetMapping("/avatar")
//    public ResponseEntity<String> getCurrentUserAvatarLink(@RequestHeader("Authorization") String token) {
//        int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
//        String avatarFileName = userProfileRepository.findById(userId).get().getAvatar();
//        String avatarLink = userProfileService.getAvatarLink(avatarFileName);
//        return ResponseEntity.ok(avatarLink);
//    }