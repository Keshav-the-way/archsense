package com.archsense.ai.service;

import com.archsense.ai.model.ArtifactContent;
import com.archsense.ai.provider.AIProvider;
import com.archsense.common.dto.AnalysisReport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AIService {

    private static final Logger log = LoggerFactory.getLogger(AIService.class);

    private final AIProvider aiProvider;

    public AIService(AIProvider aiProvider) {
        this.aiProvider = aiProvider;
    }

    public AnalysisReport analyzeArchitecture(List<ArtifactContent> artifacts, String projectId, String analysisId) {
        log.info("Starting AI analysis for analysisId: {} with {} artifacts", analysisId, artifacts.size());

        if (artifacts == null || artifacts.isEmpty()) {
            throw new IllegalArgumentException("No artifacts provided for analysis");
        }

        AnalysisReport report = aiProvider.analyze(artifacts, projectId, analysisId);

        log.info("AI analysis completed for analysisId: {}", analysisId);
        return report;
    }
}