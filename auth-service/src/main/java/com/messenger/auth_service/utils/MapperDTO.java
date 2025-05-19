package com.messenger.auth_service.utils;

import com.messenger.auth_service.dto.DeviceDTO;
import com.messenger.auth_service.dto.DeviceInfo;
import com.messenger.auth_service.models.UserDevice;
import org.springframework.stereotype.Component;

@Component
public class MapperDTO {

    public DeviceDTO mapToDeviceDTO(UserDevice device, boolean isCurrentDevice) {
        DeviceDTO dto = new DeviceDTO();
        dto.setId(device.getId());
        dto.setDeviceName(device.getDeviceName());
        dto.setDeviceType(device.getDeviceType());
        dto.setOsName(device.getOsName());
        dto.setOsVersion(device.getOsVersion());
        dto.setBrowserName(device.getBrowserName());
        dto.setBrowserVersion(device.getBrowserVersion());
        dto.setLastLoginDate(device.getLastLogin());
        dto.setIpAddress(device.getIpAddress());
        dto.setTrusted(device.isTrusted());
        dto.setCurrentDevice(isCurrentDevice);
        return dto;
    }

}
