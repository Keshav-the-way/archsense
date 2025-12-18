package com.archsense.common.dto.response;

public record AuthResponse(
        String token,
        UserResponse user
) {
}