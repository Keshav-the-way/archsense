package com.archsense.common.event;

import java.time.Instant;
import java.util.List;

public record AnalysisRequestedEvent(
        String analysisId,
        String projectId,
        String userId,
        List<String> artifactIds,
        Instant requestedAt
) {
}