package com.archsense.analysis.service;

import com.archsense.analysis.model.Analysis;
import com.archsense.analysis.model.AnalysisStatus;
import com.archsense.analysis.repository.AnalysisRepository;
import com.archsense.common.dto.request.CreateAnalysisRequest;
import com.archsense.common.dto.response.AnalysisResponse;
import com.archsense.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AnalysisService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisService.class);

    private final AnalysisRepository analysisRepository;
    private final RestTemplate restTemplate;

    @Value("${services.executor.base-url:http://localhost:8085}")
    private String executorServiceUrl;

    public AnalysisService(AnalysisRepository analysisRepository, RestTemplate restTemplate) {
        this.analysisRepository = analysisRepository;
        this.restTemplate = restTemplate;
    }

    public AnalysisResponse create(String projectId, String userId, CreateAnalysisRequest request) {
        log.info("Creating analysis for project: {}, user: {}", projectId, userId);

        // NEW: Get previous analysis for versioning
        List<Analysis> previousAnalyses = analysisRepository
                .findByUserIdAndProjectIdOrderByCreatedAtDesc(userId, projectId);

        Integer newVersion = 1;
        String previousAnalysisId = null;

        if (!previousAnalyses.isEmpty()) {
            Analysis lastAnalysis = previousAnalyses.get(0);
            newVersion = (lastAnalysis.getVersion() != null ? lastAnalysis.getVersion() : 0) + 1;
            previousAnalysisId = lastAnalysis.getId();
        }

        Analysis analysis = new Analysis();
        analysis.setUserId(userId);
        analysis.setProjectId(projectId);
        analysis.setArtifactIds(request.artifactIds());
        analysis.setStatus(AnalysisStatus.PENDING);
        analysis.setVersion(newVersion);  // NEW
        analysis.setPreviousAnalysisId(previousAnalysisId);  // NEW
        analysis.setCreatedAt(Instant.now());
        analysis.setUpdatedAt(Instant.now());

        Analysis saved = analysisRepository.save(analysis);
        log.info("Analysis created with ID: {}, Version: {}", saved.getId(), saved.getVersion());

        triggerAnalysisExecution(saved);

        return toResponse(saved);
    }

    private void triggerAnalysisExecution(Analysis analysis) {
        try {
            String url = executorServiceUrl + "/internal/execute";

            Map<String, Object> payload = new HashMap<>();
            payload.put("analysisId", analysis.getId());
            payload.put("projectId", analysis.getProjectId());
            payload.put("userId", analysis.getUserId());
            payload.put("artifactIds", analysis.getArtifactIds());
            payload.put("version", analysis.getVersion());  // NEW
            payload.put("previousAnalysisId", analysis.getPreviousAnalysisId());  // NEW

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            log.info("Triggering analysis execution for analysisId: {} (version {})",
                    analysis.getId(), analysis.getVersion());

            new Thread(() -> {
                try {
                    restTemplate.postForEntity(url, entity, String.class);
                } catch (Exception e) {
                    log.error("Failed to trigger analysis execution for analysisId: {}", analysis.getId(), e);
                }
            }).start();

        } catch (Exception e) {
            log.error("Error triggering analysis execution", e);
        }
    }

    public List<AnalysisResponse> listByProject(String projectId, String userId) {
        return analysisRepository.findByUserIdAndProjectIdOrderByCreatedAtDesc(userId, projectId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AnalysisResponse getById(String id, String userId) {
        Analysis analysis = analysisRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));
        return toResponse(analysis);
    }

    public void updateStatusInProgress(String analysisId) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));
        analysis.setStatus(AnalysisStatus.IN_PROGRESS);
        analysis.setUpdatedAt(Instant.now());
        analysisRepository.save(analysis);
        log.info("Analysis status updated to IN_PROGRESS: {}", analysisId);
    }

    public void updateStatusCompleted(String analysisId, String reportUrl) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));
        analysis.setStatus(AnalysisStatus.COMPLETED);
        analysis.setReportUrl(reportUrl);
        analysis.setCompletedAt(Instant.now());
        analysis.setUpdatedAt(Instant.now());
        analysisRepository.save(analysis);
        log.info("Analysis completed: {}", analysisId);
    }

    public void updateStatusFailed(String analysisId, String error) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));
        analysis.setStatus(AnalysisStatus.FAILED);
        analysis.setCompletedAt(Instant.now());
        analysis.setUpdatedAt(Instant.now());
        analysisRepository.save(analysis);
        log.error("Analysis failed: {} - {}", analysisId, error);
    }

    public void updateAnalysisStatus(String analysisId, AnalysisStatus status, String reportUrl) {
        Analysis analysis = analysisRepository.findById(analysisId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));

        analysis.setStatus(status);
        analysis.setReportUrl(reportUrl);
        analysis.setUpdatedAt(Instant.now());

        if (status == AnalysisStatus.COMPLETED || status == AnalysisStatus.FAILED) {
            analysis.setCompletedAt(Instant.now());
        }

        analysisRepository.save(analysis);
        log.info("Analysis status updated: {} -> {}", analysisId, status);
    }

    public void deleteByProject(String projectId) {
        List<Analysis> analyses = analysisRepository.findByUserIdAndProjectIdOrderByCreatedAtDesc(null, projectId);
        analysisRepository.deleteAll(analyses);
        log.info("Deleted {} analyses for project: {}", analyses.size(), projectId);
    }

    public String getAnalysisReport(String userId, String analysisId) {
        Analysis analysis = analysisRepository.findByIdAndUserId(analysisId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));

        if (analysis.getStatus() != AnalysisStatus.COMPLETED) {
            throw new IllegalStateException("Analysis is not completed yet. Current status: " + analysis.getStatus());
        }

        if (analysis.getReportUrl() == null) {
            throw new IllegalStateException("Report URL is not available");
        }

        return analysis.getReportUrl();
    }

    private AnalysisResponse toResponse(Analysis analysis) {
        com.archsense.common.constant.AnalysisStatus commonStatus =
                com.archsense.common.constant.AnalysisStatus.valueOf(analysis.getStatus().name());

        // Convert domain EvolutionMetrics to DTO EvolutionMetrics
        AnalysisResponse.EvolutionMetrics dtoMetrics = null;
        if (analysis.getEvolutionMetrics() != null) {
            dtoMetrics = new AnalysisResponse.EvolutionMetrics(
                    analysis.getEvolutionMetrics().getIssuesResolved(),
                    analysis.getEvolutionMetrics().getNewIssues(),
                    analysis.getEvolutionMetrics().getIssuesRegressed(),
                    analysis.getEvolutionMetrics().getImprovementScore()
            );
        }

        return new AnalysisResponse(
                analysis.getId(),
                analysis.getUserId(),
                analysis.getProjectId(),
                analysis.getArtifactIds(),
                commonStatus,
                analysis.getCreatedAt(),
                analysis.getUpdatedAt(),
                analysis.getReportUrl(),
                analysis.getVersion(),  // NEW
                analysis.getPreviousAnalysisId(),  // NEW
                dtoMetrics  // NEW
        );
    }

    public String fetchReportContent(String reportUrl) {
        try {
            String url = executorServiceUrl + "/internal/reports?path=" + reportUrl;
            log.info("Fetching report content from: {}", url);

            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getBody();
        } catch (Exception e) {
            log.error("Failed to fetch report content from: {}", reportUrl, e);
            throw new RuntimeException("Failed to fetch report content", e);
        }
    }
}