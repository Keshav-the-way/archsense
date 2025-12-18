package com.archsense.executor.controller;

import com.archsense.executor.storage.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/internal/reports")
public class InternalReportController {

    private static final Logger log = LoggerFactory.getLogger(InternalReportController.class);

    @Autowired(required = false)
    private com.archsense.executor.storage.StorageService localStorageService;

    @Autowired(required = false)
    private com.archsense.executor.storage.S3StorageService s3StorageService;

    @GetMapping
    public ResponseEntity<String> getReport(@RequestParam("path") String reportPath) {
        log.info("Fetching report: {}", reportPath);

        try {
            String reportContent;

            if (s3StorageService != null) {
                reportContent = s3StorageService.retrieveReport(reportPath);
            } else if (localStorageService != null) {
                reportContent = localStorageService.retrieveReport(reportPath);
            } else {
                throw new RuntimeException("No storage service available");
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(reportContent);
        } catch (Exception e) {
            log.error("Failed to retrieve report: {}", reportPath, e);
            return ResponseEntity.status(500)
                    .body("{\"error\":\"Failed to retrieve report\"}");
        }
    }
}