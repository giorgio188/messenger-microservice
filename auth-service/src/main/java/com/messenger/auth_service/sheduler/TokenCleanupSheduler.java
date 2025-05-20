package com.messenger.auth_service.sheduler;

import com.messenger.auth_service.repositories.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TokenCleanupSheduler {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteExpiredTokens();
    }
}
