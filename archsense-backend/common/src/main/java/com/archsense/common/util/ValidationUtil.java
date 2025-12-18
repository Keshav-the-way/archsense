package com.archsense.common.util;

import com.archsense.common.exception.ValidationException;

import java.util.regex.Pattern;

public class ValidationUtil {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );

    private ValidationUtil() {
    }

    public static void validateEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException("Invalid email format");
        }
    }

    public static void validateNotBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new ValidationException(fieldName + " cannot be blank");
        }
    }

    public static void validateLength(String value, String fieldName, int min, int max) {
        if (value != null && (value.length() < min || value.length() > max)) {
            throw new ValidationException(
                    fieldName + " must be between " + min + " and " + max + " characters"
            );
        }
    }

    public static void validatePositive(long value, String fieldName) {
        if (value <= 0) {
            throw new ValidationException(fieldName + " must be positive");
        }
    }
}