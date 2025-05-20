package com.messenger.auth_service.exception;

public class UnauthorizedDeviceAccessException extends RuntimeException {
    public UnauthorizedDeviceAccessException(String message) {
        super(message);
    }
}
