package com.messenger.auth_service.repositories;

import com.messenger.auth_service.models.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Integer> {

    Optional<RefreshToken> findByTokenAndRevokedAndDeviceId(
            String token, boolean revoked, long deviceId);

    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = :revoked WHERE r.tokenId = :tokenId")
    void updateRevokedByTokenId(@Param("tokenId") String tokenId, @Param("revoked") boolean revoked);

    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = :revoked WHERE r.userId = :userId")
    void updateRevokedByUserId(@Param("userId") int userId, @Param("revoked") boolean revoked);

    @Transactional
    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = :revoked " +
            "WHERE r.userId = :userId AND r.deviceId = :deviceId")
    void updateRevokedByUserIdAndDeviceId(
            @Param("userId") int userId,
            @Param("deviceId") long deviceId,
            @Param("revoked") boolean revoked);

    @Transactional
    @Modifying
    @Query("DELETE FROM RefreshToken r WHERE r.expiryDate < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}
