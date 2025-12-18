package com.archsense.common.dto.response;

import java.time.Instant;

public record UserResponse(
        String id,
        String email,
        String name,
        Instant createdAt
) {
}