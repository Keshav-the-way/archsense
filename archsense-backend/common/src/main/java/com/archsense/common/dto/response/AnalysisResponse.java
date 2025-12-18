package com.archsense.common.dto.response;

import com.archsense.common.constant.AnalysisStatus;

import java.time.Instant;
import java.util.List;

public record AnalysisResponse(
        String id,
        String userId,
        String projectId,
        List<String> artifactIds,
        AnalysisStatus status,
        Instant createdAt,
        Instant updatedAt,
        String reportUrl,
        Integer version,
        String previousAnalysisId,
        EvolutionMetrics evolutionMetrics
) {
    public record EvolutionMetrics(
            Integer issuesResolved,
            Integer newIssues,
            Integer issuesRegressed,
            Double improvementScore
    ) {}
}