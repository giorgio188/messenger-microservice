package com.messenger.auth_service.services;


import com.messenger.auth_service.models.UserProfile;
import com.messenger.auth_service.repositories.UserProfileRepository;
import com.messenger.auth_service.security.UserProfileDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileDetailsService implements UserDetailsService {

    private final UserProfileRepository userProfileRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<UserProfile> userProfile = userProfileRepository.findByUsername(username);
        if (userProfile.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return new UserProfileDetails(userProfile.get());
    }
}
