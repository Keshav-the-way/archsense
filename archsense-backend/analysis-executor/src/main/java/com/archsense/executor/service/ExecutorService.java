package com.archsense.executor.service;

import com.archsense.executor.ai.ClaudeAnalysisEngine;
import com.archsense.executor.ai.MockAnalysisEngine;
import com.archsense.executor.model.AnalysisReport;
import com.archsense.executor.model.Artifact;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ExecutorService {

    private static final Logger log = LoggerFactory.getLogger(ExecutorService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${services.analysis.base-url}")
    private String analysisServiceUrl;

    @Value("${services.artifact.base-url}")
    private String artifactServiceUrl;

    @Autowired(required = false)
    private com.archsense.executor.storage.StorageService localStorageService;

    @Autowired(required = false)
    private com.archsense.executor.storage.S3StorageService s3StorageService;

    @Autowired(required = false)
    private MockAnalysisEngine mockAnalysisEngine;

    @Autowired(required = false)
    private ClaudeAnalysisEngine claudeAnalysisEngine;

    public ExecutorService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public void executeAnalysisSync(String analysisId, String projectId, String userId,
                                    List<String> artifactIds,
                                    Integer version,
                                    String previousAnalysisId) {
        log.info("Executing analysis: {} (version {})", analysisId, version);

        try {
            // Update status to IN_PROGRESS
            updateAnalysisStatus(analysisId, "IN_PROGRESS", null);

            // Fetch artifacts - NOTE: passes userId as second parameter
            List<Artifact> artifacts = fetchArtifacts(artifactIds, userId);
            log.info("Fetched {} artifacts for analysis", artifacts.size());

            if (artifacts.isEmpty()) {
                throw new RuntimeException("No artifacts found for analysis");
            }

            // NEW: Build context
            Map<String, Object> context = buildAnalysisContext(
                    projectId, userId, version, previousAnalysisId);

            // Run AI analysis with context
            log.info("Performing AI analysis for analysisId: {}", analysisId);
            AnalysisReport report = performAnalysis(artifacts, context);

            // Set version
            report.setVersion(version);
            report.setAnalysisId(analysisId);

            // NEW: Calculate evolution metrics if previous analysis exists
            if (previousAnalysisId != null && report.getEvolutionAnalysis() != null) {
                calculateAndSetEvolutionMetrics(analysisId, report.getEvolutionAnalysis());
            }

            // Store report - NOTE: passes projectId and userId
            String reportPath = storeReport(analysisId, projectId, userId, report);
            log.info("Report stored at: {}", reportPath);

            // Update analysis status to COMPLETED
            updateAnalysisStatus(analysisId, "COMPLETED", reportPath);

            log.info("Analysis execution completed successfully: {}", analysisId);

        } catch (Exception e) {
            log.error("Analysis execution failed: {}", analysisId, e);
            updateAnalysisStatus(analysisId, "FAILED", null);
            throw new RuntimeException("Analysis execution failed", e);
        }
    }

    private AnalysisReport performAnalysis(List<Artifact> artifacts, Map<String, Object> context) {
        if (claudeAnalysisEngine != null) {
            log.info("Using Claude AI for analysis");
            return claudeAnalysisEngine.analyze(artifacts, context);
        } else if (mockAnalysisEngine != null) {
            log.info("Using Mock analysis");
            return mockAnalysisEngine.analyze(artifacts, context);
        } else {
            throw new RuntimeException("No analysis engine available");
        }
    }

    private List<Artifact> fetchArtifacts(List<String> artifactIds, String userId) {
        String url = artifactServiceUrl + "/internal/artifacts/batch?ids=" + String.join(",", artifactIds);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Id", userId);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Artifact>> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                new ParameterizedTypeReference<>() {}
        );

        return response.getBody();
    }

    private String storeReport(String analysisId, String projectId, String userId, AnalysisReport report) {
        try {
            String reportJson = objectMapper.writeValueAsString(report);
            String fileName = analysisId + "/report.json";

            if (s3StorageService != null) {
                log.info("Storing report in S3");
                return s3StorageService.storeReport(userId, projectId, fileName, reportJson);
            } else if (localStorageService != null) {
                log.info("Storing report locally");
                return localStorageService.storeReport(userId, projectId, fileName, reportJson);
            } else {
                throw new RuntimeException("No storage service available");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to store report", e);
        }
    }

    private void updateAnalysisStatus(String analysisId, String status, String reportUrl) {
        String url = analysisServiceUrl + "/internal/analyses/" + analysisId + "/status";

        Map<String, String> body = new HashMap<>();
        body.put("status", status);
        if (reportUrl != null) {
            body.put("reportUrl", reportUrl);
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        restTemplate.put(url, entity);
    }

    private Map<String, Object> buildAnalysisContext(String projectId, String userId,
                                                     Integer version, String previousAnalysisId) {
        Map<String, Object> context = new HashMap<>();
        context.put("version", version);

        log.info("Building context for version {}, previousAnalysisId: {}", version, previousAnalysisId);

        // Fetch project constraints
        try {
            String url = "http://localhost:8082/api/projects/" + projectId + "?userId=" + userId;
            HttpHeaders headers = new HttpHeaders();
            headers.set("X-User-Id", userId);
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, Map.class);

            if (response.getBody() != null && response.getBody().get("constraints") != null) {
                context.put("constraints", response.getBody().get("constraints"));
                log.info("Loaded project constraints: {}", response.getBody().get("constraints"));
            }
        } catch (Exception e) {
            log.warn("Could not fetch project constraints: {}", e.getMessage());
        }

        // Fetch previous report if exists
        if (previousAnalysisId != null && version != null && version > 1) {
            try {
                log.info("Attempting to fetch previous report for analysisId: {}", previousAnalysisId);
                String previousReport = fetchPreviousReport(previousAnalysisId);

                if (previousReport != null && !previousReport.trim().isEmpty()) {
                    log.info("Successfully fetched previous report, length: {} chars", previousReport.length());

                    // Parse the report to verify it's valid JSON
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode reportNode = mapper.readTree(previousReport);

                        // Log key info from previous report
                        log.info("Previous report summary - Version: {}, Issues: {}, Pattern: {}",
                                reportNode.path("version").asInt(),
                                reportNode.path("issues").size(),
                                reportNode.path("architecturePattern").asText());

                        context.put("previousReport", previousReport);
                        log.info("✓ Previous report added to context successfully");

                    } catch (Exception parseEx) {
                        log.error("Previous report is not valid JSON: {}", parseEx.getMessage());
                        log.debug("Report content: {}", previousReport.substring(0, Math.min(500, previousReport.length())));
                    }

                } else {
                    log.warn("Previous report is null or empty for analysisId: {}", previousAnalysisId);
                }
            } catch (Exception e) {
                log.error("Failed to fetch previous report for analysisId: {}", previousAnalysisId, e);
            }
        } else {
            log.info("No previous analysis comparison needed - version: {}, previousId: {}", version, previousAnalysisId);
        }

        return context;
    }

    private String fetchPreviousReport(String previousAnalysisId) {
        try {
            String url = analysisServiceUrl + "/internal/analyses/" + previousAnalysisId;
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getBody() != null && response.getBody().get("reportUrl") != null) {
                String reportUrl = (String) response.getBody().get("reportUrl");

                // Fetch the actual report content
                if (s3StorageService != null) {
                    return s3StorageService.retrieveReport(reportUrl);
                } else if (localStorageService != null) {
                    return localStorageService.retrieveReport(reportUrl);
                }
            }
        } catch (Exception e) {
            log.error("Failed to fetch previous report", e);
        }
        return null;
    }

    private void calculateAndSetEvolutionMetrics(String analysisId,
                                                 AnalysisReport.EvolutionAnalysis evolution) {
        try {
            int resolved = evolution.getResolvedIssues() != null ?
                    evolution.getResolvedIssues().size() : 0;
            int newIssues = evolution.getNewIssues() != null ?
                    evolution.getNewIssues().size() : 0;
            int regressed = evolution.getRegressedIssues() != null ?
                    evolution.getRegressedIssues().size() : 0;

            // Calculate improvement score (0-100)
            double improvementScore = 50.0; // Neutral baseline
            if (resolved > 0 || newIssues > 0 || regressed > 0) {
                improvementScore = 50.0 + (resolved * 10.0) - (newIssues * 5.0) - (regressed * 15.0);
                improvementScore = Math.max(0, Math.min(100, improvementScore));
            }

            // Update analysis with metrics via HTTP
            String url = analysisServiceUrl + "/internal/analyses/" + analysisId + "/evolution";
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("issuesResolved", resolved);
            metrics.put("newIssues", newIssues);
            metrics.put("issuesRegressed", regressed);
            metrics.put("improvementScore", improvementScore);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(metrics, headers);

            restTemplate.put(url, entity);
            log.info("Evolution metrics updated for analysis: {}", analysisId);

        } catch (Exception e) {
            log.error("Failed to update evolution metrics", e);
        }
    }
}