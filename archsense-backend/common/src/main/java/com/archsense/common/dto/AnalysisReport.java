package com.archsense.common.dto;

import java.time.Instant;
import java.util.List;

public record AnalysisReport(
        String analysisId,
        String projectId,
        Summary summary,
        List<Issue> issues,
        List<Recommendation> recommendations,
        Instant generatedAt
) {
    public record Summary(
            String overview,
            int totalComponents,
            int totalConnections,
            String architecturePattern
    ) {
    }

    public record Issue(
            String severity,
            String category,
            String description,
            String location
    ) {
    }

    public record Recommendation(
            String priority,
            String category,
            String description,
            String rationale
    ) {
    }
}