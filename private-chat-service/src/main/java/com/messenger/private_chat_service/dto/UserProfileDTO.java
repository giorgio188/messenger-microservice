package com.messenger.private_chat_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserProfileDTO {

    @Size(min = 8, max = 60)
    private String username;

    @Size(min = 3, max = 60)
    private String nickname;

    @Size(min = 10, max = 10)
    @Pattern(regexp = "^9\\d{9}$")
    private String phoneNumber;

    @Email
    private String email;

}
