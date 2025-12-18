package com.archsense.common.dto.response;

import com.archsense.common.constant.ArtifactType;

import java.time.Instant;

public record ArtifactResponse(
        String id,
        String projectId,
        String userId,
        String name,
        ArtifactType type,
        long size,
        String s3Key,
        Instant uploadedAt
) {
}