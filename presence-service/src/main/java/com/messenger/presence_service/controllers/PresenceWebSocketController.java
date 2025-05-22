package com.messenger.presence_service.controllers;

import com.messenger.presence_service.models.PresenceStatus;
import com.messenger.presence_service.services.PresenceService;
import com.messenger.presence_service.services.TypingNotificationService;
import com.messenger.presence_service.services.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class PresenceWebSocketController {

    private final PresenceService presenceService;
    private final TypingNotificationService typingNotificationService;
    private final UserSessionService userSessionService;

    @MessageMapping("/presence.status")
    public void handleStatusUpdate(
            @Payload Map<String, Object> payload,
            @Header("X-User-Id") int userId,
            @Header(value = "X-Device-Id", required = false) String deviceId) {

        try {
            String statusStr = (String) payload.get("status");
            PresenceStatus status = PresenceStatus.valueOf(statusStr.toUpperCase());

            presenceService.updateUserStatus(userId, status, deviceId, "WEBSOCKET");
        } catch (Exception e) {
        }
    }

    @MessageMapping("/presence.status-message")
    public void handleStatusMessageUpdate(
            @Payload Map<String, Object> payload,
            @Header("X-User-Id") int userId) {

        try {
            String statusMessage = (String) payload.get("statusMessage");

            presenceService.updateStatusMessage(userId, statusMessage);
        } catch (Exception e) {
        }
    }

    @MessageMapping("/chat.typing.start")
    public void handleTypingStart(
            @Payload Map<String, Object> payload,
            @Header("X-User-Id") int userId,
            @Header(value = "X-Device-Id", required = false) String deviceId) {

        try {
            int chatId = ((Number) payload.get("chatId")).intValue();

            typingNotificationService.processTypingStart(userId, chatId, deviceId);
        } catch (Exception e) {
        }
    }

    @MessageMapping("/chat.typing.stop")
    public void handleTypingStop(
            @Payload Map<String, Object> payload,
            @Header("X-User-Id") int userId,
            @Header(value = "X-Device-Id", required = false) String deviceId) {

        try {
            int chatId = ((Number) payload.get("chatId")).intValue();

            typingNotificationService.processTypingStop(userId, chatId, deviceId);
        } catch (Exception e) {
        }
    }

    @MessageMapping("/heartbeat")
    public void handleHeartbeat(
            @Header("X-User-Id") int userId,
            @Header(value = "X-Device-Id", required = false) String deviceId,
            SimpMessageHeaderAccessor headerAccessor) {

        userSessionService.processHeartbeat(userId, deviceId);
    }

}
