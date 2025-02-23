package com.messenger.chat_service.exceptions;

public class ChatValidationException extends RuntimeException {
  public ChatValidationException(String message) {
    super(message);
  }
}
