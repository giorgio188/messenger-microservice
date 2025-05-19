package com.messenger.auth_service.utils;

import com.messenger.auth_service.dto.DeviceInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import nl.basjes.parse.useragent.UserAgent;
import nl.basjes.parse.useragent.UserAgentAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ClientInfoExtractor {

    private final Logger logger = LoggerFactory.getLogger(ClientInfoExtractor.class);
    private final UserAgentAnalyzer userAgentAnalyzer;

    public DeviceInfo extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        String ipAddress = extractIpAddress(request);

        DeviceInfo deviceInfo = new DeviceInfo();
        deviceInfo.setIpAddress(ipAddress);
        deviceInfo.setUserAgent(userAgent);

        String fingerprint = request.getHeader("X-Device-Fingerprint");
        if (fingerprint == null || fingerprint.isEmpty()) {
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
        try {
            // Создаем строку из данных, которые у нас есть
            String dataToHash = userAgent + "|" + ipAddress;

            // Создаем хеш SHA-256
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(dataToHash.getBytes());

            // Кодируем хеш в base64url для более компактного представления
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString();
        }
    }

    // получение типа устройства и браузера из юзер агента
    private void parseUserAgent(String userAgentString, DeviceInfo deviceInfo) {
        if (userAgentString == null || userAgentString.isEmpty()) {
            setDefaultDeviceInfo(deviceInfo);
            return;
        }

        try {
            UserAgent agent = userAgentAnalyzer.parse(userAgentString);

            // Основная информация об ОС
            String osName = agent.getValue("OperatingSystemName");
            String osVersion = agent.getValue("OperatingSystemVersion");

            // Информация о браузере
            String browserName = agent.getValue("AgentName");
            String browserVersion = agent.getValue("AgentVersion");

            // Информация об устройстве
            String deviceBrand = agent.getValue("DeviceBrand");
            String deviceModel = agent.getValue("DeviceName");
            String deviceClass = agent.getValue("DeviceClass");

            // Определяем тип устройства на основе класса устройства
            String deviceType = mapDeviceType(deviceClass, agent);

            // Форматируем название устройства
            String formattedDeviceName = formatDeviceName(deviceBrand, deviceModel, osName, deviceClass);

            // Заполняем основную информацию
            deviceInfo.setDeviceName(formattedDeviceName);
            deviceInfo.setDeviceType(deviceType);
            deviceInfo.setDeviceBrand(deviceBrand);
            deviceInfo.setDeviceModel(deviceModel);
            deviceInfo.setOsName(osName);
            deviceInfo.setOsVersion(osVersion);
            deviceInfo.setBrowserName(browserName);
            deviceInfo.setBrowserVersion(browserVersion);

            // Определяем тип устройства на основе класса устройства
            boolean isMobile = deviceClass.equalsIgnoreCase("Phone") ||
                    deviceClass.equalsIgnoreCase("Mobile") ||
                    deviceClass.equalsIgnoreCase("Smartphone");

            boolean isTablet = deviceClass.equalsIgnoreCase("Tablet");

            boolean isDesktop = deviceClass.equalsIgnoreCase("Desktop") ||
                    deviceClass.equalsIgnoreCase("Laptop");

            deviceInfo.setMobile(isMobile);
            deviceInfo.setTablet(isTablet);
            deviceInfo.setDesktop(isDesktop);

            // Информация о движке
            String layoutEngine = agent.getValue("LayoutEngineName");
            String layoutEngineVersion = agent.getValue("LayoutEngineVersion");
            deviceInfo.setLayoutEngine(layoutEngine + " " + layoutEngineVersion);

            // Для отладки можно выводить все доступные поля
            if (logger.isDebugEnabled()) {
                logger.debug("YAUAA Analysis: {}", agent.toJson());

                // Выводим все доступные поля
                for (String fieldName : agent.getAvailableFieldNamesSorted()) {
                    logger.debug("Field {}: {}", fieldName, agent.getValue(fieldName));
                }
            }

        } catch (Exception e) {
            setDefaultDeviceInfo(deviceInfo);
            logger.error("Error parsing user agent with YAUAA: {}", e.getMessage(), e);
        }
    }

    /**
     * Преобразует класс устройства YAUAA в наш тип устройства
     */
    private String mapDeviceType(String deviceClass, UserAgent agent) {
        if (deviceClass == null || deviceClass.isEmpty() || "Unknown".equals(deviceClass)) {
            return "Unknown";
        }

        switch (deviceClass.toLowerCase()) {
            case "phone":
            case "smartphone":
            case "mobile":
            case "feature phone":
                return "Mobile";

            case "tablet":
            case "phablet":
                return "Tablet";

            case "desktop":
            case "laptop":
                return "Desktop";

            case "tv":
            case "smart display":
            case "set-top box":
                return "TV";

            case "game console":
            case "handheld game console":
                return "Game Console";

            case "watch":
            case "wearable":
                return "Wearable";

            case "e-reader":
                return "E-Reader";

            case "car":
            case "car browser":
                return "Car System";

            case "robot":
            case "spy":
            case "hacker":
                return "Bot";

            default:
                return deviceClass; // Используем оригинальный класс, если не знаем, как его преобразовать
        }
    }

    /**
     * Форматирует название устройства в читаемый вид
     */
    private String formatDeviceName(String brand, String model, String osName, String deviceClass) {
        // Если бренд и модель известны, используем их
        if (brand != null && !brand.isEmpty() && !"Unknown".equals(brand) &&
                model != null && !model.isEmpty() && !"Unknown".equals(model)) {
            return brand + " " + model;
        }

        // Если известен только бренд
        if (brand != null && !brand.isEmpty() && !"Unknown".equals(brand)) {
            if (deviceClass != null && !deviceClass.isEmpty() && !"Unknown".equals(deviceClass)) {
                return brand + " " + deviceClass;
            }
            return brand + " Device";
        }

        // Если известна только модель
        if (model != null && !model.isEmpty() && !"Unknown".equals(model)) {
            return model;
        }

        // Если ничего не известно, используем информацию об ОС и типе устройства
        if (osName != null && !osName.isEmpty() && !"Unknown".equals(osName)) {
            if ("iOS".equals(osName)) {
                return "iOS Device";
            } else if ("Android".equals(osName)) {
                return "Android Device";
            } else if (osName.contains("Windows")) {
                if (deviceClass != null && (deviceClass.equalsIgnoreCase("Phone") ||
                        deviceClass.equalsIgnoreCase("Mobile"))) {
                    return "Windows Phone";
                }
                return "Windows PC";
            } else if (osName.contains("Mac")) {
                return "Mac";
            } else if ("Linux".equals(osName)) {
                return "Linux PC";
            } else {
                return osName + " Device";
            }
        }

        // Если и ОС неизвестна, используем тип устройства
        if (deviceClass != null && !deviceClass.isEmpty() && !"Unknown".equals(deviceClass)) {
            return deviceClass;
        }

        // Если всё неизвестно
        return "Unknown Device";
    }

    private void setDefaultDeviceInfo(DeviceInfo deviceInfo) {
        deviceInfo.setDeviceName("Unknown Device");
        deviceInfo.setDeviceType("Unknown");
        deviceInfo.setDeviceBrand("Unknown");
        deviceInfo.setDeviceModel("Unknown");
        deviceInfo.setOsName("Unknown");
        deviceInfo.setOsVersion("");
        deviceInfo.setBrowserName("Unknown");
        deviceInfo.setBrowserVersion("");
        deviceInfo.setBrowserEngine("Unknown");
        deviceInfo.setLayoutEngine("Unknown");
        deviceInfo.setMobile(false);
        deviceInfo.setTablet(false);
        deviceInfo.setDesktop(false);
        deviceInfo.setNetworkType("Unknown");
        deviceInfo.setScreenResolution("Unknown");
    }

}
