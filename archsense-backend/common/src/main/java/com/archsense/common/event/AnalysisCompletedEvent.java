package com.archsense.common.event;

import java.time.Instant;

public record AnalysisCompletedEvent(
        String analysisId,
        String projectId,
        String userId,
        String reportS3Key,
        Instant completedAt
) {
}