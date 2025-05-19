package com.messenger.auth_service.services;

import com.messenger.auth_service.dto.DeviceDTO;
import com.messenger.auth_service.exception.DeviceNotFoundException;
import com.messenger.auth_service.exception.UnauthorizedDeviceAccessException;
import com.messenger.auth_service.models.UserDevice;
import com.messenger.auth_service.repositories.UserDeviceRepository;
import com.messenger.auth_service.security.JWTUtil;
import com.messenger.auth_service.utils.MapperDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final UserDeviceRepository userDeviceRepository;
    private final JWTUtil jwtUtil;
    private final MapperDTO mapperDTO;

    public List<DeviceDTO> getUserDevices(int userId, int currentDeviceId) {
        List<UserDevice> userDevices = userDeviceRepository.findByUserId(userId);

        return userDevices.stream()
                .map(device -> mapperDTO.mapToDeviceDTO(device, device.getId() == currentDeviceId))
                .collect(Collectors.toList());
    }

    @Transactional
    public void revokeDevice(int userId, int deviceId, int currentDeviceId) {
        if (deviceId == currentDeviceId) {
            throw new IllegalArgumentException("Cannot revoke current device through this method");
        }

        UserDevice device = userDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        if (device.getUserId() != userId) {
            throw new UnauthorizedDeviceAccessException("Not authorized to access this device");
        }

        jwtUtil.revokeUserDeviceTokens(userId, deviceId);

        device.setTrusted(false);
        userDeviceRepository.save(device);
    }

    @Transactional
    public void revokeAllDevicesExceptCurrent(int userId, int currentDeviceId) {
        List<UserDevice> userDevices = userDeviceRepository.findByUserId(userId);

        userDevices.stream()
                .filter(device -> device.getId() != currentDeviceId)
                .forEach(device -> {jwtUtil.revokeUserDeviceTokens(userId, device.getId());
                        device.setTrusted(false);
                });
        userDeviceRepository.saveAll(userDevices);
    }

    @Transactional
    public void revokeAllDevices(int userId) {
        List<UserDevice> userDevices = userDeviceRepository.findByUserId(userId);

        userDevices.forEach(device -> {
            jwtUtil.revokeUserDeviceTokens(userId, device.getId());
            device.setTrusted(false);
        });
        userDeviceRepository.saveAll(userDevices);
    }

    @Transactional
    public DeviceDTO updateDeviceName(int userId, int deviceId, String newName) {
        UserDevice device = userDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        if (device.getUserId() != userId) {
            throw new UnauthorizedDeviceAccessException("Not authorized to access this device");
        }

        device.setDeviceName(newName);
        device = userDeviceRepository.save(device);

        return mapperDTO.mapToDeviceDTO(device, false);
    }

    @Transactional
    public DeviceDTO updateDeviceTrusted(int userId, int deviceId, boolean trusted) {
        UserDevice device = userDeviceRepository.findById(deviceId)
                .orElseThrow(() -> new DeviceNotFoundException("Device not found: " + deviceId));

        if (device.getUserId() != userId) {
            throw new UnauthorizedDeviceAccessException("Not authorized to access this device");
        }

        // Если устройство помечается как ненадежное, отзываем его токены
        if (!trusted && device.isTrusted()) {
            jwtUtil.revokeUserDeviceTokens(userId, deviceId);
        }

        device.setTrusted(trusted);
        device = userDeviceRepository.save(device);

        return mapperDTO.mapToDeviceDTO(device, false);
    }

    public boolean isDeviceOwnedByUser(int userId, int deviceId) {
        return userDeviceRepository.findById(deviceId)
                .map(device -> device.getUserId() == userId)
                .orElse(false);
    }

    @Transactional
    public void cleanupOldDevices(int days) {
        // Вычисляем дату, старше которой устройства считаются устаревшими
        java.time.LocalDateTime cutoffDate = java.time.LocalDateTime.now().minusDays(days);
        java.util.Date date = java.sql.Timestamp.valueOf(cutoffDate);

        // Находим и удаляем устаревшие устройства
        List<UserDevice> oldDevices = userDeviceRepository.findByLastLoginDateBefore(date);

        // Перед удалением отзываем токены для каждого устройства
        oldDevices.forEach(device ->
                jwtUtil.revokeUserDeviceTokens(device.getUserId(), device.getId()));

        userDeviceRepository.deleteAll(oldDevices);
    }

}
