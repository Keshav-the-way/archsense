package com.archsense.common.dto.response;

import com.archsense.common.dto.ProjectConstraints;

import java.time.Instant;

public record ProjectResponse(
        String id,
        String userId,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt,
        ProjectConstraints constraints
) {
}