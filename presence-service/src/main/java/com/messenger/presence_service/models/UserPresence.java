package com.messenger.presence_service.models;

import lombok.Data;

import java.io.Serializable;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Data
public class UserPresence implements Serializable {

    private static final long serialVersionUID = 1L;

    private int userId;

    private PresenceStatus status;

    private Instant lastActivity;

    private Instant lastSeen;

    private String statusMessage;

    private Set<String> activeDevices = new HashSet<>();

    public boolean addDevice(String deviceId) {
        return activeDevices.add(deviceId);
    }

    public boolean removeDevice(String deviceId) {
        return activeDevices.remove(deviceId);
    }

    public boolean hasActiveDevices() {
        return !activeDevices.isEmpty();
    }

    public void updateLastActivity() {
        this.lastActivity = Instant.now();
    }

    public void updateLastSeen() {
        this.lastSeen = Instant.now();
    }
}
