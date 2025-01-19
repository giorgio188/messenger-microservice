package com.messenger.auth_service.utils;


import com.messenger.auth_service.models.UserProfile;
import com.messenger.auth_service.repositories.UserProfileRepository;
import com.messenger.auth_service.services.UserProfileDetailsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProfileValidator implements Validator {

    private final UserProfileRepository userProfileRepository;
    private final UserProfileDetailsService userProfileDetailsService;

    @Override
    public boolean supports(Class<?> clazz) {
        return UserProfile.class.equals(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        log.info("Loading user profile");
        UserProfile userProfile = (UserProfile) target;
        log.info("Validating of user's details");

        log.info("Validation of empty fields");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "username", "field.required", "Username is required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "password", "field.required", "Password is required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "nickname", "field.required", "Nickname is required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phoneNumber", "field.required", "Phone number is required");
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, "email", "field.required", "Email is required");

        log.info("Validating of fields length");
        if (userProfile.getUsername() != null && userProfile.getUsername().length() < 8) {
            errors.rejectValue("username", "field.minlength", "Username must be at least 8 characters long");
        }
        if (userProfile.getUsername() != null && userProfile.getUsername().length() > 60) {
            errors.rejectValue("username", "field.maxlength", "Username must be at most 60 characters long");
        }

        if (userProfile.getNickname() != null && userProfile.getNickname().length() < 3) {
            errors.rejectValue("nickname", "field.minlength", "Nickname must be at least 3 characters long");
        }
        if (userProfile.getNickname() != null && userProfile.getNickname().length() > 60) {
            errors.rejectValue("nickname", "field.maxlength", "Nickname must be at most 60 characters long");
        }

        if (userProfile.getPhoneNumber() != null && userProfile.getPhoneNumber().length() != 10) {
            errors.rejectValue("phoneNumber", "field.length", "Phone number must be exactly 10 digits long");
        }

        log.info("Validating of email");
        if (userProfile.getEmail() != null && !userProfile.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            errors.rejectValue("email", "field.invalid", "Email format is invalid");
        }


        log.info("Checking for unique fields");
        if (userProfileRepository.existsByUsername(userProfile.getUsername())) {
            errors.rejectValue("username", "field.duplicate", "Username already exists");
        }

        if (userProfileRepository.existsByEmail(userProfile.getEmail())) {
            errors.rejectValue("email", "field.duplicate", "Email already exists");
        }

        if (userProfileRepository.existsByPhoneNumber(userProfile.getPhoneNumber())) {
            errors.rejectValue("phoneNumber", "field.duplicate", "Phone number already exists");
        }
    }
}
