package com.archsense.common.event;

import java.time.Instant;

public record AnalysisFailedEvent(
        String analysisId,
        String projectId,
        String userId,
        String error,
        Instant failedAt
) {
}