package com.archsense.common.event;

import com.archsense.common.constant.ArtifactType;

import java.time.Instant;

public record ArtifactUploadedEvent(
        String artifactId,
        String projectId,
        String userId,
        String name,
        ArtifactType type,
        String s3Key,
        long size,
        Instant uploadedAt
) {
}