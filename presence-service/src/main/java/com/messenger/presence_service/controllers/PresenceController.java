package com.messenger.presence_service.controllers;

import com.messenger.presence_service.dto.UserPresenceDTO;
import com.messenger.presence_service.models.PresenceStatus;
import com.messenger.presence_service.models.UserPresence;
import com.messenger.presence_service.services.PresenceService;
import com.messenger.presence_service.utils.MapperDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/presence")
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;
    private final MapperDTO mapperDTO;

    @GetMapping("/{userId}")
    public ResponseEntity<UserPresenceDTO> getUserPresence(@PathVariable int userId){
        UserPresence userPresence = presenceService.getUserPresence(userId);

        if(userPresence == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(mapperDTO.convertToUserPresenceDTO(userPresence));
    }

    @GetMapping("/bulk")
    public ResponseEntity<List<UserPresenceDTO>> getMultipleUsersPresence(@RequestParam List<Integer> userIds) {
        List<UserPresenceDTO> presenceList = presenceService.getMultipleUsersPresence(userIds);
        return ResponseEntity.ok(presenceList);
    }


    @PostMapping("/status")
    public ResponseEntity<UserPresenceDTO> updateUserStatus(
            @RequestHeader("X-User-Id") int userId,
            @RequestParam PresenceStatus status,
            @RequestParam(required = false) String deviceId) {

        UserPresence updatedPresence = presenceService.updateUserStatus(userId, status, deviceId, "API");

        return ResponseEntity.ok(mapperDTO.convertToUserPresenceDTO(updatedPresence));
    }

    @PostMapping("/status-message")
    public ResponseEntity<UserPresenceDTO> updateStatusMessage(
            @RequestHeader("X-User-Id") int userId,
            @RequestBody String statusMessage) {

        UserPresence updatedPresence = presenceService.updateStatusMessage(userId, statusMessage);

        return ResponseEntity.ok(mapperDTO.convertToUserPresenceDTO(updatedPresence));
    }

    @GetMapping("/by-status")
    public ResponseEntity<List<Integer>> getUsersByStatus(@RequestParam PresenceStatus status) {
        List<Integer> userIds = presenceService.getUsersByStatus(status);
        return ResponseEntity.ok(userIds);
    }
}
