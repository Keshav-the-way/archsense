package com.archsense.common.event;

import java.time.Instant;

public record ArtifactDeletedEvent(
        String artifactId,
        String projectId,
        String userId,
        String s3Key,
        Instant deletedAt
) {
}