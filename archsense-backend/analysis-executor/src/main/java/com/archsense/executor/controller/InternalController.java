package com.archsense.executor.controller;

import com.archsense.executor.service.ExecutorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/internal")
public class InternalController {

    private static final Logger log = LoggerFactory.getLogger(InternalController.class);

    private final ExecutorService executorService;

    public InternalController(ExecutorService executorService) {
        this.executorService = executorService;
    }

    @PostMapping("/execute")
    public ResponseEntity<Map<String, String>> executeAnalysis(@RequestBody Map<String, Object> payload) {
        String analysisId = (String) payload.get("analysisId");
        String projectId = (String) payload.get("projectId");
        String userId = (String) payload.get("userId");
        @SuppressWarnings("unchecked")
        List<String> artifactIds = (List<String>) payload.get("artifactIds");
        Integer version = (Integer) payload.get("version");  // NEW
        String previousAnalysisId = (String) payload.get("previousAnalysisId");  // NEW

        log.info("Received analysis execution request for analysisId: {} (version {})",
                analysisId, version);

        try {
            executorService.executeAnalysisSync(analysisId, projectId, userId, artifactIds,
                    version, previousAnalysisId);
            return ResponseEntity.ok(Map.of("status", "success"));
        } catch (Exception e) {
            log.error("Failed to execute analysis: {}", analysisId, e);
            return ResponseEntity.status(500).body(Map.of("status", "error", "message", e.getMessage()));
        }
    }
}