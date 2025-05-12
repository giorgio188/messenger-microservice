package com.messenger.auth_service.utils;

import com.messenger.auth_service.dto.DeviceInfo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ClientInfoExtractor {

    public DeviceInfo extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = extractIpAddress(request);

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setIpAddress(ipAddress);
        deviceInfo.setUserAgent(userAgent);

        String fingerprint = request.getHeader("X-Device-Fingerprint");
        if (fingerprint != null || fingerprint.isEmpty()) {
            fingerprint = generateFingerprint(userAgent, ipAddress);
        }

        deviceInfo.setDeviceId(fingerprint);

        parseUserAgent(userAgent, deviceInfo);
        return deviceInfo;
    }

    // извлечение айпи адреса из запроса с учетом использования прокси
    private String extractIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");

        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }

        // Если получен список IP-адресов через прокси, берем первый
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }

        return ipAddress;
    }

    // генерация fingerprint
    private String generateFingerprint(String userAgent, String ipAddress) {
        return UUID.nameUUIDFromBytes((userAgent + ipAddress).getBytes()).toString();
    }

    // получение типа устройства и браузера из юзер агента
    private void parseUserAgent(String userAgent, DeviceInfo deviceInfo) {
        if (userAgent == null || userAgent.isEmpty()) {
            deviceInfo.setDeviceName("Unknown Device");
            deviceInfo.setDeviceType("Unknown");
            return;
        }
        //TODO реализовать с помощью UA parser

    }
}
