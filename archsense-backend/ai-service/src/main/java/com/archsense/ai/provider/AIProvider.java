package com.archsense.ai.provider;

import com.archsense.ai.model.ArtifactContent;
import com.archsense.common.dto.AnalysisReport;

import java.util.List;

public interface AIProvider {

    AnalysisReport analyze(List<ArtifactContent> artifacts, String projectId, String analysisId);
}