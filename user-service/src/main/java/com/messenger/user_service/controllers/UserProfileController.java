package com.messenger.user_service.controllers;


import com.messenger.user_service.dto.UserProfileDTO;
import com.messenger.user_service.dto.UserProfilePageDTO;
import com.messenger.user_service.models.UserProfile;
import com.messenger.user_service.models.enums.ProfileStatus;
import com.messenger.user_service.repositories.UserProfileRepository;
import com.messenger.user_service.services.UserProfileService;
import com.messenger.user_service.utils.JWTUtil;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
    private final JWTUtil jwtUtil;
    private final ModelMapper modelMapper;
    private final UserProfileRepository userProfileRepository;


    @GetMapping("/{userId}")
    public ResponseEntity<UserProfilePageDTO> getUserProfile(@PathVariable int userId) {
        UserProfile userProfile = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(modelMapper.map(userProfile, UserProfilePageDTO.class));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfile> getCurrentUserProfile(@RequestHeader("Authorization") String token) {
        int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        UserProfile userProfile = userProfileService.getUserProfile(userId);
        return ResponseEntity.ok(userProfile);
    }

    @PatchMapping("/update")
    public ResponseEntity<UserProfile> updateUserProfile(@RequestBody UserProfileDTO userProfileDTO,
                                                         @RequestHeader("Authorization") String token) {
        int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        UserProfile updatedUserProfile = modelMapper.map(userProfileDTO, UserProfile.class);
        userProfileService.updateUserProfile(userId, updatedUserProfile);
        return ResponseEntity.ok(updatedUserProfile);
    }

    @PostMapping("/updatePassword")
    public void updateUserProfilePassword(@RequestHeader("Authorization") String token,
                                          @RequestParam String newPassword) {
        int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        userProfileService.changePassword(userId, newPassword);
    }

    @DeleteMapping("/delete")
    @ResponseStatus(HttpStatus.FOUND)
    public ResponseEntity<HttpStatus> deleteUserProfile(@RequestHeader("Authorization") String token) {
        int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        userProfileService.deleteUserProfile(userId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @GetMapping("/search") public ResponseEntity<List<UserProfile>> searchUsers(@RequestParam String query) {
        List<UserProfile> searchResults = userProfileService.searchUsers(query);
        return ResponseEntity.ok(searchResults);
    }

    @PostMapping("/avatar")
    public ResponseEntity<?> addUserAvatar(@RequestHeader("Authorization") String token,
                                                     @RequestParam MultipartFile file) {
        try {
            String getExtension = file.getContentType();
            if (getExtension == null || !getExtension.startsWith("image/")) {
                return ResponseEntity.badRequest().body("You can upload only image as avatar");
            }
            int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            String avatarURL = userProfileService.uploadAvatar(userId, file);
            return ResponseEntity.ok(avatarURL);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Could not upload avatar" + e.getMessage());
        }
    }

    @DeleteMapping("/avatar")
    public ResponseEntity<?> deleteUserAvatar(@RequestHeader("Authorization") String token) {
        int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
        userProfileService.deleteAvatar(userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/avatar")
    public ResponseEntity<String> getUserAvatar(@RequestHeader("Authorization") String token) {
        int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
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
        String token = headerAccessor.getFirstNativeHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            userProfileService.setUserOnlineStatus(userId, ProfileStatus.ONLINE);
        }
    }

    @MessageMapping("/user.disconnect")
    public void handleUserDisconnect(SimpMessageHeaderAccessor headerAccessor) {
        String token = headerAccessor.getFirstNativeHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            int userId = jwtUtil.extractUserId(token.replace("Bearer ", ""));
            userProfileService.setUserOnlineStatus(userId, ProfileStatus.OFFLINE);
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