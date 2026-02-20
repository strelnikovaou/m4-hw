package org.strelnikova.m4hw.validation;

import org.springframework.stereotype.Component;
import org.strelnikova.m4hw.controller.dto.UserRequestDTO;
import org.strelnikova.m4hw.exception.ValidationException;

import java.util.regex.Pattern;

@Component
public class UserValidator {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    private static final int MAX_AGE = 150;

    public void validate(UserRequestDTO request) {
        validateName(request.name());
        validateEmail(request.email());
        validateAge(request.age());
    }

    private void validateName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name cannot be empty");
        }
    }

    private void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format: " + email);
        }
    }

    private void validateAge(Integer age) {
        if (age == null || age < 0 || age > MAX_AGE) {
            throw new ValidationException("Age must be between 0 and " + MAX_AGE);
        }
    }
}
