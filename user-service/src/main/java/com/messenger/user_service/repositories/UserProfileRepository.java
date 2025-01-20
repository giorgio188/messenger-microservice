package com.messenger.user_service.repositories;

import com.messenger.user_service.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    Optional<UserProfile> findById(int id);
    Optional<UserProfile> findByUsername(String username);
    List<UserProfile> findByUsernameContainingOrNicknameContaining(String username, String nickname);
}
