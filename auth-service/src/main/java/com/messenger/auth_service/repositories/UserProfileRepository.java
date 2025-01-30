package com.messenger.auth_service.repositories;

import com.messenger.auth_service.models.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProfileRepository extends JpaRepository<UserProfile, Integer> {
    Optional<UserProfile> findById(int id);
    Optional<UserProfile> findByUsername(String username);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPhoneNumber(String phoneNumber);
    List<UserProfile> findByUsernameContainingOrNicknameContaining(String username, String nickname);
}
