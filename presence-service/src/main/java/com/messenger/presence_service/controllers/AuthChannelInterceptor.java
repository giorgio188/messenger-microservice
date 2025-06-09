package com.messenger.presence_service.controllers;

import com.messenger.presence_service.events.UserConnectEvent;
import com.messenger.presence_service.events.UserDisconnectEvent;
import com.messenger.presence_service.services.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthChannelInterceptor implements ChannelInterceptor {

    private final ApplicationEventPublisher eventPublisher;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor != null) {
            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                String userIdStr = accessor.getFirstNativeHeader("X-User-Id");
                String deviceId = accessor.getFirstNativeHeader("X-Device-Id");

                if (userIdStr != null) {
                    try {
                        int userId = Integer.parseInt(userIdStr);
                        eventPublisher.publishEvent(new UserConnectEvent(this, userId, deviceId));
                        accessor.setUser(() -> userIdStr);
                    } catch (NumberFormatException e) {
                    }
                }
            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
                if (accessor.getUser() != null) {
                    String userIdStr = accessor.getUser().getName();
                    String deviceId = accessor.getFirstNativeHeader("X-Device-Id");

                    try {
                        int userId = Integer.parseInt(userIdStr);
                        eventPublisher.publishEvent(new UserDisconnectEvent(this, userId, deviceId));
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }

        return message;
    }


}

//    private final UserSessionService userSessionService;
//
//    @Override
//    public Message<?> preSend(Message<?> message, MessageChannel channel) {
//        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
//        if (accessor != null) {
//            if (StompCommand.CONNECT.equals(accessor.getCommand())) {
//                String userIdStr = accessor.getFirstNativeHeader("X-User-Id");
//                String deviceId = accessor.getFirstNativeHeader("X-Device-Id");
//
//                if (userIdStr != null) {
//                    try {
//                        int userId = Integer.parseInt(userIdStr);
//
//                        userSessionService.handleUserConnect(userId, deviceId);
//
//                        accessor.setUser(() -> userIdStr);
//                    } catch (NumberFormatException e) {
//                    }
//                }
//            } else if (StompCommand.DISCONNECT.equals(accessor.getCommand())) {
//                if (accessor.getUser() != null) {
//                    String userIdStr = accessor.getUser().getName();
//                    String deviceId = accessor.getFirstNativeHeader("X-Device-Id");
//
//                    try {
//                        int userId = Integer.parseInt(userIdStr);
//
//                        userSessionService.handleUserDisconnect(userId, deviceId);
//                    } catch (NumberFormatException e) {
//                    }
//                }
//            }
//        }
//
//        return message;
//    }
