package com.messenger.auth_service.controllers;

import com.messenger.auth_service.security.JWTUtil;
import com.messenger.auth_service.services.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/devices")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;
    private final JWTUtil jwtUtil;

    //TODO дописать контроллер. Не забыть про x-user-id в заголовке
    // добавить эти пути в апи гв
}
