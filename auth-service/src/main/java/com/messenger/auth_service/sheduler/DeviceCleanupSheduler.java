package com.messenger.auth_service.sheduler;

import com.messenger.auth_service.services.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceCleanupSheduler {

    private final DeviceService deviceService;

    @Value("${app.device.cleanup-days}")
    private int cleanupAfterDays;

    @Scheduled(cron = "0 0 2 * * 0")
    public void cleanupOldDevices() {
        deviceService.cleanupOldDevices(cleanupAfterDays);
    }

}
