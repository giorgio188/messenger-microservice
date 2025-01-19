package com.messenger.auth_service.services;


import com.messenger.auth_service.models.UserProfile;
import com.messenger.auth_service.models.enums.ProfileStatus;
import com.messenger.auth_service.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationService {

    private final UserProfileRepository userProfileRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void register(UserProfile userProfile) {
        userProfile.setPassword(passwordEncoder.encode(userProfile.getPassword()));
        log.info("Registering user: " + userProfile.getUsername());
        userProfile.setCreatedAt(LocalDateTime.now());
        userProfile.setStatus(ProfileStatus.ONLINE);
        userProfileRepository.save(userProfile);
    }

    @Transactional
    public void changePassword(int userId, String newPassword) {
        Optional<UserProfile> userProfile = userProfileRepository.findById(userId);
        if (userProfile.isPresent()) {
            userProfile.get().setPassword(passwordEncoder.encode(newPassword));
            userProfileRepository.save(userProfile.get());
        }
    }

}
