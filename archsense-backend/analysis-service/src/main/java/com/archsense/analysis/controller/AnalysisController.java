package com.archsense.analysis.controller;

import com.archsense.analysis.model.Analysis;
import com.archsense.analysis.repository.AnalysisRepository;
import com.archsense.analysis.service.AnalysisService;
import com.archsense.common.dto.request.CreateAnalysisRequest;
import com.archsense.common.dto.response.AnalysisResponse;
import com.archsense.common.exception.ResourceNotFoundException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.archsense.common.constant.AnalysisStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AnalysisController {

    private static final Logger log = LoggerFactory.getLogger(AnalysisController.class);

    private final AnalysisService analysisService;
    private final AnalysisRepository analysisRepository;

    public AnalysisController(AnalysisService analysisService, AnalysisRepository analysisRepository) {
        this.analysisService = analysisService;
        this.analysisRepository = analysisRepository;
    }

    @PostMapping("/projects/{projectId}/analyses")
    public ResponseEntity<AnalysisResponse> createAnalysis(
            @PathVariable String projectId,
            @RequestHeader("X-User-Id") String userId,
            @Valid @RequestBody CreateAnalysisRequest request) {

        log.info("POST /projects/{}/analyses - userId: {}", projectId, userId);
        AnalysisResponse response = analysisService.create(projectId, userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/analyses")
    public ResponseEntity<List<AnalysisResponse>> listAnalyses(
            @PathVariable String projectId,
            @RequestHeader("X-User-Id") String userId) {

        log.info("GET /projects/{}/analyses - userId: {}", projectId, userId);
        List<AnalysisResponse> response = analysisService.listByProject(projectId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analyses/{id}")
    public ResponseEntity<AnalysisResponse> getAnalysis(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {

        log.info("GET /analyses/{} - userId: {}", id, userId);
        AnalysisResponse response = analysisService.getById(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/analyses/{id}/report")
    public ResponseEntity<String> getAnalysisReport(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {

        log.info("GET /analyses/{}/report - userId: {}", id, userId);
        // Get the analysis to verify ownership and status
        AnalysisResponse analysis = analysisService.getById(id, userId);

        if (analysis.status() != AnalysisStatus.COMPLETED) {
            throw new IllegalStateException("Analysis has not completed yet");
        }

        if (analysis.reportUrl() == null) {
            throw new IllegalStateException("Report not available");
        }

        // Fetch the actual report content from executor service
        String reportContent = analysisService.fetchReportContent(analysis.reportUrl());
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(reportContent);
    }

    @GetMapping("/{id}/evolution")
    public ResponseEntity<Map<String, Object>> getEvolutionMetrics(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {

        Analysis analysis = analysisRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Analysis not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("version", analysis.getVersion());
        response.put("previousAnalysisId", analysis.getPreviousAnalysisId());
        response.put("evolutionMetrics", analysis.getEvolutionMetrics());

        return ResponseEntity.ok(response);
    }
}