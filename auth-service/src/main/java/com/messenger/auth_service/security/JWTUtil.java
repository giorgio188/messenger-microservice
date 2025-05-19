package com.messenger.auth_service.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.messenger.auth_service.dto.DeviceInfo;
import com.messenger.auth_service.dto.TokenInfo;
import com.messenger.auth_service.dto.TokenPair;
import com.messenger.auth_service.exception.TokenRevokedException;
import com.messenger.auth_service.models.RefreshToken;
import com.messenger.auth_service.models.UserDevice;
import com.messenger.auth_service.repositories.RefreshTokenRepository;
import com.messenger.auth_service.repositories.UserDeviceRepository;
import com.messenger.auth_service.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Component
@RequiredArgsConstructor
public class JWTUtil {

    @Value("${spring.jwt.secret}")
    private String secret;

    @Value("${spring.jwt.access-token-expiration-minutes}")
    private int accessTokenExpirationMinutes;

    @Value("${spring.jwt.refresh-token-expiration-days}")
    private int refreshTokenExpirationDays;

    private final AuthService authService;
    private final RedisTemplate<String, String> redisTemplate;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserDeviceRepository userDeviceRepository;

    /**
     * Генерирует пару токенов (access и refresh) с информацией об устройстве
     */
    public TokenPair generateTokenPair(String username, int userId, DeviceInfo deviceInfo) {
        // Сначала регистрируем или получаем существующее устройство
        UserDevice device = registerOrUpdateDevice(userId, deviceInfo);

        String accessToken = generateAccessToken(username, userId, device.getId());
        String refreshToken = generateRefreshToken(username, userId, device.getId());

        // Сохраняем refresh токен в БД
        saveRefreshToken(refreshToken, userId, device.getId());

        return new TokenPair(accessToken, refreshToken);
    }

    /**
     * Регистрирует новое устройство или обновляет существующее
     */
    private UserDevice registerOrUpdateDevice(int userId, DeviceInfo deviceInfo) {
        // Ищем устройство по fingerprint
        Optional<UserDevice> existingDevice = userDeviceRepository
                .findByUserIdAndDeviceDetails(userId, deviceInfo.getDeviceId());

        if (existingDevice.isPresent()) {
            // Обновляем существующее устройство
            UserDevice device = existingDevice.get();
            device.setLastLogin(LocalDateTime.now());
            device.setDeviceName(deviceInfo.getDeviceName());
            device.setIpAddress(deviceInfo.getIpAddress());
            return userDeviceRepository.save(device);
        } else {
            // Создаем новое устройство
            UserDevice newDevice = new UserDevice();
            newDevice.setUserId(userId);
            newDevice.setDeviceDetails(deviceInfo.getDeviceId());
            newDevice.setDeviceName(deviceInfo.getDeviceName());
            newDevice.setIpAddress(deviceInfo.getIpAddress());
            newDevice.setLastLogin(LocalDateTime.now());
            return userDeviceRepository.save(newDevice);
        }
    }

    /**
     * Генерирует короткоживущий access токен с информацией об устройстве
     */
    public String generateAccessToken(String username, int userId, long deviceId) {
        Date expirationDate = Date.from(ZonedDateTime.now()
                .plusMinutes(accessTokenExpirationMinutes).toInstant());

        return JWT.create()
                .withSubject("User authentication")
                .withClaim("id", userId)
                .withClaim("username", username)
                .withClaim("device_id", deviceId) // Добавляем ID устройства
                .withJWTId(UUID.randomUUID().toString()) // Уникальный ID для возможности отзыва
                .withIssuedAt(new Date())
                .withIssuer("messenger")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret)); // Оставляем прежний алгоритм
    }

    /**
     * Генерирует долгоживущий refresh токен с привязкой к устройству
     */
    private String generateRefreshToken(String username, int userId, long deviceId) {
        Date expirationDate = Date.from(ZonedDateTime.now()
                .plusDays(refreshTokenExpirationDays).toInstant());

        return JWT.create()
                .withSubject("Refresh token")
                .withClaim("id", userId)
                .withClaim("username", username)
                .withClaim("device_id", deviceId) // Добавляем ID устройства
                .withJWTId(UUID.randomUUID().toString())
                .withIssuedAt(new Date())
                .withIssuer("messenger")
                .withExpiresAt(expirationDate)
                .sign(Algorithm.HMAC256(secret)); // Оставляем прежний алгоритм
    }

    /**
     * Сохраняет refresh токен в БД с привязкой к устройству
     */
    private void saveRefreshToken(String token, int userId, int deviceId) {
        String tokenId = JWT.decode(token).getId();
        Date expiryDate = JWT.decode(token).getExpiresAt();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setTokenId(tokenId);
        refreshToken.setToken(hashToken(token)); // Храним хэш токена, не сам токен
        refreshToken.setUserId(userId);
        refreshToken.setDeviceId(deviceId); // Привязываем к устройству
        //TODO пофиксить
        refreshToken.setExpiryDate(expiryDate);
        refreshToken.setRevoked(false);

        refreshTokenRepository.save(refreshToken);
    }

    /**
     * Хэширует токен перед сохранением для дополнительной безопасности
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Hashing algorithm not found", e);
        }
    }

    /**
     * Обновляет пару токенов, используя refresh токен, с проверкой устройства
     */
    public TokenPair refreshTokens(String refreshToken) {
        // Проверяем, что токен валидный и не отозван
        if (isTokenRevoked(refreshToken)) {
            throw new TokenRevokedException("Refresh token has been revoked");
        }

        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withSubject("Refresh token")
                    .withIssuer("messenger")
                    .build();

            DecodedJWT jwt = verifier.verify(refreshToken);
            String username = jwt.getClaim("username").asString();
            int userId = jwt.getClaim("id").asInt();
            int deviceId = jwt.getClaim("device_id").asInt();
            String tokenId = jwt.getId();

            // Проверяем токен в БД с учетом устройства
            String hashedToken = hashToken(refreshToken);
            Optional<RefreshToken> storedToken = refreshTokenRepository.findByTokenAndRevokedAndDeviceId(
                    hashedToken, false, deviceId);

            if (storedToken.isEmpty()) {
                throw new TokenRevokedException("Refresh token not found, revoked, or device mismatch");
            }

            // Отзываем текущий refresh токен (одноразовое использование)
            revokeRefreshToken(tokenId);

            // Получаем информацию об устройстве
            UserDevice device = userDeviceRepository.findById(deviceId)
                    .orElseThrow(() -> new TokenRevokedException("Device not found"));

            // Обновляем дату последнего входа
            device.setLastLogin(LocalDateTime.now());
            userDeviceRepository.save(device);

            // Генерируем новую пару токенов
            String newAccessToken = generateAccessToken(username, userId, deviceId);
            String newRefreshToken = generateRefreshToken(username, userId, deviceId);

            // Сохраняем новый refresh токен
            saveRefreshToken(newRefreshToken, userId, deviceId);

            return new TokenPair(newAccessToken, newRefreshToken);

        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Invalid refresh token", e);
        }
    }

    /**
     * Проверяет access токен
     */
    public Optional<String> verifyToken(String token) {
        // Проверяем, не в черном ли списке токен
        if (isTokenRevoked(token)) {
            return Optional.empty();
        }

        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withSubject("User authentication")
                    .withIssuer("messenger")
                    .build();

            DecodedJWT jwt = verifier.verify(token);
            String username = jwt.getClaim("username").asString();
            int userId = jwt.getClaim("id").asInt();
            long deviceId = jwt.getClaim("device_id").asLong();

            // Проверяем, не отозвано ли устройство, с которого был выпущен токен
            if (authService.isDeviceRevoked(userId, deviceId)) {
                return Optional.empty();
            }

            return Optional.of(username);
        } catch (JWTVerificationException e) {
            return Optional.empty();
        }
    }

    /**
     * Проверяет, отозван ли токен
     */
    private boolean isTokenRevoked(String token) {
        try {
            String tokenId = JWT.decode(token).getId();
            return Boolean.TRUE.equals(redisTemplate.hasKey("revoked_token:" + tokenId));
        } catch (Exception e) {
            return true; // При любой ошибке считаем токен отозванным
        }
    }

    /**
     * Отзывает access токен, добавляя его в черный список
     */
    public void revokeAccessToken(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            String tokenId = jwt.getId();

            // Вычисляем оставшееся время жизни токена
            Date expiryDate = jwt.getExpiresAt();
            long ttlMillis = expiryDate.getTime() - System.currentTimeMillis();

            if (ttlMillis > 0) {
                // Добавляем в Redis с TTL равным оставшемуся времени жизни
                redisTemplate.opsForValue().set(
                        "revoked_token:" + tokenId,
                        "revoked",
                        ttlMillis,
                        TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    /**
     * Отзывает refresh токен
     */
    public void revokeRefreshToken(String tokenId) {
        refreshTokenRepository.updateRevokedByTokenId(tokenId, true);
    }

    /**
     * Отзывает все refresh токены пользователя (например, при смене пароля)
     */
    public void revokeAllUserRefreshTokens(int userId) {
        refreshTokenRepository.updateRevokedByUserId(userId, true);
    }

    /**
     * Отзывает токены для конкретного устройства пользователя
     */
    public void revokeUserDeviceTokens(int userId, long deviceId) {
        // Отзываем все refresh токены для данного устройства
        refreshTokenRepository.updateRevokedByUserIdAndDeviceId(userId, deviceId, true);

        // Находим и отзываем все активные access токены, связанные с этим устройством
        revokeActiveAccessTokensForDevice(userId, deviceId);

        // Отмечаем устройство как недоверенное
        userDeviceRepository.findById((int)deviceId).ifPresent(device -> {
            device.setTrusted(false);
            userDeviceRepository.save(device);
        });
    }

    /**
     * Находит и отзывает все активные access токены для заданного устройства
     */
    private void revokeActiveAccessTokensForDevice(int userId, long deviceId) {
        // Создаем новую таблицу в Redis для хранения отозванных устройств
        String deviceRevokedKey = String.format("revoked_device:%d:%d", userId, deviceId);

        // Устанавливаем в Redis отметку, что всё устройство отозвано
        // TTL устанавливаем на максимальный срок жизни access токена
        authService.markDeviceAsRevoked(deviceRevokedKey, TimeUnit.MINUTES.toMillis(accessTokenExpirationMinutes));
    }

    /**
     * Извлекает ID пользователя из токена
     */
    public int extractUserId(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("messenger")
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("id").asInt();
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    /**
     * Извлекает ID устройства из токена
     */
    public int extractDeviceId(String token) {
        try {
            DecodedJWT jwt = JWT.decode(token);
            return jwt.getClaim("device_id").asInt();
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    /**
     * Извлекает информацию о токене для использования в AuthenticationFilter
     */
    public TokenInfo extractTokenInfo(String token) {
        try {
            // Проверяем, не отозван ли токен
            if (isTokenRevoked(token)) {
                throw new TokenRevokedException("Token has been revoked");
            }

            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
                    .withIssuer("messenger")
                    .build();
            DecodedJWT jwt = verifier.verify(token);

            int userId = jwt.getClaim("id").asInt();
            long deviceId = jwt.getClaim("device_id").asLong();

            // Проверяем, не отозвано ли устройство
            if (authService.isDeviceRevoked(userId, deviceId)) {
                throw new TokenRevokedException("Device has been revoked");
            }

            TokenInfo tokenInfo = new TokenInfo();
            tokenInfo.setUserId(userId);
            tokenInfo.setExpirationTime(jwt.getExpiresAt().toInstant().getEpochSecond());

            return tokenInfo;
        } catch (JWTVerificationException e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }
}

//    @Value("${spring.jwt.secret}")
//    private String secret;
//
//    public String generateToken(String username, int id) {
//        Date expirationDate = Date.from(ZonedDateTime.now().plusMinutes(6000).toInstant());
//        return JWT.create()
//                .withSubject("User authentication")
//                .withClaim("id", id)
//                .withClaim("username", username)
//                .withIssuedAt(new Date())
//                .withIssuer("admin")
//                .withExpiresAt(expirationDate)
//                .sign(Algorithm.HMAC256(secret));
//    }
//
//    public Optional<String> verifyToken(String token) {
//        try {
//            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
//                    .withSubject("User authentication")
//                    .withIssuer("admin")
//                    .build();
//
//            DecodedJWT jwt = verifier.verify(token);
//            String username = jwt.getClaim("username").asString();
//            return Optional.of(username);
//        } catch (JWTVerificationException e) {
//            return Optional.empty();
//        }
//    }
//
//    public int extractUserId(String token) {
//        try {
//            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
//                    .withSubject("User authentication")
//                    .withIssuer("admin")
//                    .build();
//            DecodedJWT jwt = verifier.verify(token);
//            return jwt.getClaim("id").asInt();
//        } catch (JWTVerificationException e) {
//            throw new IllegalArgumentException("Invalid token", e);
//        }
//    }
//
//    public TokenInfo extractTokenInfo(String token) {
//        try {
//            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(secret))
//                    .withSubject("User authentication")
//                    .withIssuer("admin")
//                    .build();
//            DecodedJWT jwt = verifier.verify(token);
//
//            TokenInfo tokenInfo = new TokenInfo();
//            tokenInfo.setUserId(jwt.getClaim("id").asInt());
//            tokenInfo.setExpirationTime(jwt.getExpiresAt().toInstant().getEpochSecond());
//
//            return tokenInfo;
//        } catch (JWTVerificationException e) {
//            throw new IllegalArgumentException("Invalid token", e);
//        }
//    }
