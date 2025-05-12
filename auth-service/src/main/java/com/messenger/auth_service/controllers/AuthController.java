package com.messenger.auth_service.controllers;


import com.messenger.auth_service.dto.*;
import com.messenger.auth_service.exception.TokenRevokedException;
import com.messenger.auth_service.models.UserProfile;
import com.messenger.auth_service.repositories.UserProfileRepository;
import com.messenger.auth_service.security.JWTUtil;
import com.messenger.auth_service.services.AuthService;
import com.messenger.auth_service.services.RegistrationService;
import com.messenger.auth_service.utils.ClientInfoExtractor;
import com.messenger.auth_service.utils.UserProfileValidator;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController()
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000")
public class AuthController {

    private final RegistrationService registrationService;
    private final JWTUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserProfileValidator userProfileValidator;
    private final UserProfileRepository userProfileRepository;
    private final ModelMapper modelMapper;
    private final ClientInfoExtractor clientInfoExtractor;

    @PostMapping("/registration")
    public ResponseEntity<?> performRegistration (@RequestBody RegistrationDTO registrationDTO,
                                                  BindingResult bindingResult, HttpServletRequest request) {
        UserProfile userProfile = modelMapper.map(registrationDTO, UserProfile.class);
        userProfileValidator.validate(userProfile, bindingResult);
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        registrationService.register(userProfile);

        DeviceInfo deviceInfo = clientInfoExtractor.extractDeviceInfo(request);


        TokenPair tokenPair = jwtUtil.generateTokenPair(
                userProfile.getUsername(),
                userProfile.getId(),
                deviceInfo);
        return ResponseEntity.ok(tokenPair);
    }

    @PostMapping("/login")
    public ResponseEntity<?> performLogin(
            @RequestBody AuthDTO authDTO, HttpServletRequest request) {

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(authDTO.getUsername(), authDTO.getPassword());

        try {
            Authentication auth =authenticationManager.authenticate(authToken);
            if (!auth.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().build();
        }

        UserProfile user = userProfileRepository.findByUsername(authDTO.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        DeviceInfo deviceInfo = clientInfoExtractor.extractDeviceInfo(request);

        TokenPair tokenPair = jwtUtil.generateTokenPair(
                authDTO.getUsername(),
                user.getId(),
                deviceInfo);

        return ResponseEntity.ok(tokenPair);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        try {
            TokenPair tokens = jwtUtil.refreshTokens(request.getRefreshToken());
            return ResponseEntity.ok(tokens);
        } catch (TokenRevokedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Invalid refresh token"));
        }
    }

    @PostMapping("/verify")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is missing");
        }

        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        try {
            TokenInfo tokenInfo = jwtUtil.extractTokenInfo(jwtToken);
            return ResponseEntity.ok(tokenInfo);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid token");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader("Authorization") String token) {
        if (token == null || token.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token is missing");
        }

        String jwtToken = token.startsWith("Bearer ") ? token.substring(7) : token;

        try {
            int userId = jwtUtil.extractUserId(jwtToken);
            int deviceId = jwtUtil.extractDeviceId(jwtToken);

            // Отзываем токены для текущего устройства
            jwtUtil.revokeUserDeviceTokens(userId, deviceId);

            // Добавляем access токен в черный список
            jwtUtil.revokeAccessToken(jwtToken);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error during logout: " + e.getMessage());
        }
    }
}
