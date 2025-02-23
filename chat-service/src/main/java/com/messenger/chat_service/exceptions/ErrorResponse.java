package com.messenger.chat_service.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.stereotype.Component;

@Data
@AllArgsConstructor
public class ErrorResponse {
    private String error;
    private String message;
}
