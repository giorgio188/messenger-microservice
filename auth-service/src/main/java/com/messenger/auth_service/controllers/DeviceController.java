package com.messenger.auth_service.controllers;

import com.messenger.auth_service.dto.DeviceDTO;
import com.messenger.auth_service.models.UserDevice;
import com.messenger.auth_service.repositories.UserDeviceRepository;
import com.messenger.auth_service.security.JWTUtil;
import com.messenger.auth_service.services.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final UserDeviceRepository deviceRepository;
    private final JWTUtil jwtUtil;

    //TODO дописать контроллер. Не забыть про x-user-id в заголовке
    // добавить эти пути в апи гв

    /**
     * Получение списка устройств пользователя
     */
    @GetMapping
    public ResponseEntity<List<DeviceDTO>> getUserDevices(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        int userId = jwtUtil.extractUserId(token);

        List<UserDevice> devices = deviceRepository.findByUserId(userId);

        // Текущее устройство
        int currentDeviceId = jwtUtil.extractDeviceId(token);

        List<DeviceDTO> deviceDTOs = devices.stream()
                .map(device -> {
                    DeviceDTO dto = new DeviceDTO();
                    dto.setId(device.getId());
                    dto.setDeviceName(device.getDeviceName());
                    dto.setLastLoginDate(device.getLastLogin());
                    dto.setIpAddress(device.getIpAddress());
                    dto.setTrusted(device.isTrusted());
                    dto.setCurrentDevice(device.getId() == currentDeviceId);
                    return dto;
                })
                .collect(Collectors.toList());

        return ResponseEntity.ok(deviceDTOs);
    }

    /**
     * Удаление устройства (выход из сессии на конкретном устройстве)
     */
    @DeleteMapping("/{deviceId}")
    public ResponseEntity<?> revokeDevice(
            @RequestHeader("Authorization") String token,
            @PathVariable int deviceId) {

        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        int userId = jwtUtil.extractUserId(token);
        long currentDeviceId = jwtUtil.extractDeviceId(token);

        // Нельзя удалить текущее устройство через этот метод
        if (deviceId == currentDeviceId) {
            return ResponseEntity.badRequest()
                    .body("Cannot revoke current device. Use /api/auth/logout instead.");
        }

        // Проверяем, принадлежит ли устройство пользователю
        boolean exists = deviceRepository.findById(deviceId)
                .map(device -> device.getUserId() == userId)
                .orElse(false);

        if (!exists) {
            return ResponseEntity.notFound().build();
        }

        // Отзываем токены для устройства
        jwtUtil.revokeUserDeviceTokens(userId, deviceId);

        return ResponseEntity.ok().build();
    }

    /**
     * Выход со всех устройств, кроме текущего
     */
    @PostMapping("/revoke-all-except-current")
    public ResponseEntity<?> revokeAllExceptCurrent(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        int userId = jwtUtil.extractUserId(token);
        long currentDeviceId = jwtUtil.extractDeviceId(token);

        List<UserDevice> devices = deviceRepository.findByUserId(userId);

        // Отзываем токены для всех устройств, кроме текущего
        devices.stream()
                .filter(device -> device.getId() != currentDeviceId)
                .forEach(device -> jwtUtil.revokeUserDeviceTokens(userId, device.getId()));

        return ResponseEntity.ok().build();
    }

    /**
     * Выход со всех устройств, включая текущее
     */
    @PostMapping("/revoke-all")
    public ResponseEntity<?> revokeAllDevices(@RequestHeader("Authorization") String token) {
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }

        int userId = jwtUtil.extractUserId(token);

        List<UserDevice> devices = deviceRepository.findByUserId(userId);

        // Отзываем токены для всех устройств
        devices.forEach(device ->
                jwtUtil.revokeUserDeviceTokens(userId, device.getId()));

        // Отзываем текущий access токен
        jwtUtil.revokeAccessToken(token);

        return ResponseEntity.ok().build();
    }
}
