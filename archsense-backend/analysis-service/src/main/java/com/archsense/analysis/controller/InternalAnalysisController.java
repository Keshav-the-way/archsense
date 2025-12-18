package com.archsense.analysis.controller;

import com.archsense.analysis.model.Analysis;
import com.archsense.analysis.model.AnalysisStatus;
import com.archsense.analysis.repository.AnalysisRepository;
import com.archsense.analysis.service.AnalysisService;
import com.archsense.common.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/internal/analyses")
public class InternalAnalysisController {

    private static final Logger log = LoggerFactory.getLogger(InternalAnalysisController.class);

    private final AnalysisService analysisService;
    private final AnalysisRepository analysisRepository;

    public InternalAnalysisController(AnalysisService analysisService, AnalysisRepository analysisRepository) {
        this.analysisService = analysisService;
        this.analysisRepository = analysisRepository;
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, String>> updateStatus(
            @PathVariable String id,
            @RequestBody Map<String, String> request) {

        String status = request.get("status");
        String reportUrl = request.get("reportUrl");

        log.info("Updating analysis status: {} -> {}", id, status);

        AnalysisStatus analysisStatus = AnalysisStatus.valueOf(status);
        analysisService.updateAnalysisStatus(id, analysisStatus, reportUrl);

        return ResponseEntity.ok(Map.of("message", "Status updated successfully"));
    }

    @PutMapping("/{id}/evolution")
    public ResponseEntity<Map<String, String>> updateEvolutionMetrics(
            @PathVariable String id,
            @RequestBody Map<String, Object> metrics) {

        Analysis analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));

        Analysis.EvolutionMetrics evolutionMetrics = new Analysis.EvolutionMetrics();
        evolutionMetrics.setIssuesResolved((Integer) metrics.get("issuesResolved"));
        evolutionMetrics.setNewIssues((Integer) metrics.get("newIssues"));
        evolutionMetrics.setIssuesRegressed((Integer) metrics.get("issuesRegressed"));
        evolutionMetrics.setImprovementScore((Double) metrics.get("improvementScore"));

        analysis.setEvolutionMetrics(evolutionMetrics);
        analysis.setUpdatedAt(Instant.now());
        analysisRepository.save(analysis);

        log.info("Evolution metrics updated for analysis: {}", id);

        return ResponseEntity.ok(Map.of("message", "Evolution metrics updated"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAnalysisById(@PathVariable String id) {
        log.info("Internal request to fetch analysis: {}", id);

        Analysis analysis = analysisRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found: " + id));

        Map<String, Object> response = new HashMap<>();
        response.put("id", analysis.getId());
        response.put("userId", analysis.getUserId());
        response.put("projectId", analysis.getProjectId());
        response.put("status", analysis.getStatus().toString());
        response.put("reportUrl", analysis.getReportUrl());
        response.put("version", analysis.getVersion());
        response.put("previousAnalysisId", analysis.getPreviousAnalysisId());
        response.put("createdAt", analysis.getCreatedAt());
        response.put("updatedAt", analysis.getUpdatedAt());

        return ResponseEntity.ok(response);
    }
}