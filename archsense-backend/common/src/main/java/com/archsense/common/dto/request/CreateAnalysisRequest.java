package com.archsense.common.dto.request;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateAnalysisRequest(
        @NotEmpty(message = "At least one artifact must be selected")
        List<String> artifactIds
) {
}