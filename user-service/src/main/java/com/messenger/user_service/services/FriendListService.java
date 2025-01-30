package com.messenger.user_service.services;

import com.messenger.user_service.models.FriendList;
import com.messenger.user_service.models.UserProfile;
import com.messenger.user_service.repositories.FriendListRepository;
import com.messenger.user_service.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FriendListService {

    private final FriendListRepository friendListRepository;
    private final UserProfileRepository userProfileRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public List<UserProfile> getFriendList(int userId) {
        UserProfile userProfile = userProfileRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        List<FriendList> friendsAsUser = friendListRepository.findByUserId(userProfile);
        List<FriendList> friendsAsFriend = friendListRepository.findByFriendId(userProfile);
        Set<UserProfile> friendsSet = new HashSet<>();
        friendsAsUser.forEach(friendList -> friendsSet.add(friendList.getFriendId()));
        friendsAsFriend.forEach(friendList -> friendsSet.add(friendList.getUserId()));
        return new ArrayList<>(friendsSet);
    }

    @Transactional
    public void addFriend(int userId, int friendId) {
        Optional<UserProfile> user = userProfileRepository.findById(userId);
        Optional<UserProfile> friend = userProfileRepository.findById(friendId);
        if (user.isPresent() && friend.isPresent()) {
            FriendList friendList = new FriendList(user.get(), friend.get(), LocalDateTime.now());
            friendListRepository.save(friendList);

            // Отправляем уведомление обоим пользователям
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "FRIEND_ADDED");
            notification.put("userId", userId);
            notification.put("friendId", friendId);
            notification.put("timestamp", LocalDateTime.now());

            // Уведомление инициатору
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId),
                    "/queue/friends",
                    notification
            );

            // Уведомление добавленному другу
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(friendId),
                    "/queue/friends",
                    notification
            );
        }
    }

    @Transactional
    public void deleteFriend(int userId, int friendToBeDeleted) {
        Optional<UserProfile> userProfile = userProfileRepository.findById(userId);
        Optional<UserProfile> friend = userProfileRepository.findById(friendToBeDeleted);
        if (userProfile.isPresent() && friend.isPresent()) {
            friendListRepository.deleteByUserIdAndFriendId(userProfile, friend);
            friendListRepository.deleteByUserIdAndFriendId(friend, userProfile);

            // Отправляем уведомление обоим пользователям
            Map<String, Object> notification = new HashMap<>();
            notification.put("type", "FRIEND_DELETED");
            notification.put("userId", userId);
            notification.put("friendId", friendToBeDeleted);
            notification.put("timestamp", LocalDateTime.now());

            // Уведомление инициатору удаления
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(userId),
                    "/queue/friends",
                    notification
            );

            // Уведомление удаленному другу
            messagingTemplate.convertAndSendToUser(
                    String.valueOf(friendToBeDeleted),
                    "/queue/friends",
                    notification
            );
        }
    }
}
