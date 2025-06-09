package com.messenger.user_service.services;

import com.messenger.user_service.dto.FriendDTO;
import com.messenger.user_service.models.UserProfile;
import com.messenger.user_service.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final S3Service s3Service;
    private final FriendListService friendListService;
    private static final String AVATAR_DIRECTORY = "avatars";
    private final PasswordEncoder passwordEncoder;
    private final PresenceServiceClient presenceServiceClient;

    public UserProfile getUserProfile(int id) {
        Optional<UserProfile> userProfile = userProfileRepository.findById(id);
        return userProfile.orElse(null);
    }

    public List<UserProfile> searchUsers(String query) {
        return userProfileRepository.findByUsernameContainingOrNicknameContaining(query, query);
    }

    @Transactional
    public void updateUserProfile(int id, UserProfile updatedUserProfile) {
        UserProfile userProfile = getUserProfile(id);
        if (userProfile != null) {
            userProfile.setNickname(updatedUserProfile.getNickname());
            userProfile.setUsername(updatedUserProfile.getUsername());
            userProfile.setEmail(updatedUserProfile.getEmail());
            userProfile.setPhoneNumber(updatedUserProfile.getPhoneNumber());
        }

        UserProfile savedProfile = userProfileRepository.save(userProfile);
        List<FriendDTO> friendList = friendListService.getFriendList(id);
        messagingTemplate.convertAndSend("/topic/user/" + id, savedProfile);
        friendList.forEach(friend ->
                messagingTemplate.convertAndSend("/topic/user/" + friend.getId() + "/friend-update", savedProfile)
        );
    }

    @Transactional
    public void deleteUserProfile(int id) {
        userProfileRepository.deleteById(id);
    }



    @Transactional
    public String uploadAvatar(int userId, MultipartFile file) {
        UserProfile userProfile = getUserProfile(userId);
        try {
            if (userProfile.getAvatar() != null && !userProfile.getAvatar().isEmpty()) {
                s3Service.deleteFile(userProfile.getAvatar());
            }
            String fileName = s3Service.uploadFile(file, AVATAR_DIRECTORY);
            userProfile.setAvatar(fileName);
            userProfileRepository.save(userProfile);
            return s3Service.getFileUrl(fileName);
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload avatar", e);
        }
    }

    @Transactional
    public void deleteAvatar(int userId) {
        UserProfile userProfile = getUserProfile(userId);
        try {
            String avatar = userProfile.getAvatar();
            if (!avatar.isEmpty() && userProfile.getAvatar() != null) {
                s3Service.deleteFile(avatar);
                userProfile.setAvatar(null);
                userProfileRepository.save(userProfile);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete avatar" + e.getMessage());
        }
    }

    public String getAvatarLink(String fileName) {
        return s3Service.getFileUrl(fileName);
    }

//    @Transactional
//    public void handleLogout(int userId) {
//
//        Map<String, Object> logoutNotification = new HashMap<>();
//        logoutNotification.put("type", "USER_LOGOUT");
//        logoutNotification.put("userId", userId);
//        logoutNotification.put("timestamp", LocalDateTime.now());
//
//        try {
//            List<FriendDTO> friendList = friendListService.getFriendList(userId);
//            friendList.forEach(friend ->
//                    messagingTemplate.convertAndSendToUser(
//                            String.valueOf(friend.getId()),
//                            "/queue/friends/logout",
//                            logoutNotification
//                    )
//            );
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to logout user " + userId, e);
//        }
//    }

    @Transactional
    public void changePassword(int userId, String newPassword) {
        Optional<UserProfile> userProfile = userProfileRepository.findById(userId);
        if (userProfile.isPresent()) {
            userProfile.get().setPassword(passwordEncoder.encode(newPassword));
            userProfileRepository.save(userProfile.get());
        }
    }

    public boolean existsById(int id) {
        return userProfileRepository.existsById(id);
    }

}
//
//@Transactional
//public void handleLogout(int userId) {
//    setUserOnlineStatus(userId, ProfileStatus.OFFLINE);
//}
// Метод для обработки отключения пользователя при разрыве WebSocket соединения
//@Transactional
//public void handleWebSocketDisconnect(int userId) {
//    setUserOnlineStatus(userId, ProfileStatus.OFFLINE);
//    log.info("User {} disconnected from WebSocket", userId);
//}
//
//// Метод для обработки подключения пользователя при установке WebSocket соединения
//@Transactional
//public void handleWebSocketConnect(int userId) {
//    setUserOnlineStatus(userId, ProfileStatus.ONLINE);
//    log.info("User {} connected via WebSocket", userId);
//}
//    @Transactional
//    public void setUserOnlineStatus(int id, ProfileStatus status) {
//        Optional<UserProfile> userProfile = userProfileRepository.findById(id);
//        if (userProfile.isPresent()) {
//            UserProfile user = userProfile.get();
//            user.setStatus(status);
//            UserProfile savedProfile = userProfileRepository.save(user);
//
//            // Создаем уведомление о смене статуса
//            Map<String, Object> statusNotification = new HashMap<>();
//            statusNotification.put("type", "STATUS_CHANGED");
//            statusNotification.put("userId", id);
//            statusNotification.put("newStatus", status);
//            statusNotification.put("timestamp", LocalDateTime.now());
//
//            messagingTemplate.convertAndSendToUser(
//                    String.valueOf(id),
//                    "/queue/status",
//                    statusNotification
//            );
//
//            List<FriendDTO> friendList = friendListService.getFriendList(id);
//            friendList.forEach(friend ->
//                    messagingTemplate.convertAndSendToUser(
//                            String.valueOf(friend.getId()),
//                            "/queue/friends/status",
//                            statusNotification
//                    )
//            );
//
//        }
//    }