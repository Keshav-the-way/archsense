package com.archsense.executor.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class StorageService {

    private static final Logger log = LoggerFactory.getLogger(StorageService.class);

    @Value("${storage.local.reports-path:./storage/reports}")
    private String reportsBasePath;

    public String storeReport(String userId, String projectId, String fileName, String content) {
        try {
            // fileName includes analysisId/report.json, so we need to create the full path
            Path fullPath = Paths.get(reportsBasePath, userId, projectId, fileName);

            // Create all parent directories
            Files.createDirectories(fullPath.getParent());

            // Write the file
            Files.writeString(fullPath, content);

            String reportUrl = "reports/" + userId + "/" + projectId + "/" + fileName;
            log.info("Report stored successfully at: {}", reportUrl);

            return reportUrl;

        } catch (IOException e) {
            log.error("Failed to store report at path: {}/{}/{}/{}",
                    reportsBasePath, userId, projectId, fileName, e);
            throw new RuntimeException("Failed to store report", e);
        }
    }

    public String retrieveReport(String reportUrl) {
        try {
            // reportUrl is like: reports/userId/projectId/analysisId/report.json
            // We need to resolve it from the base storage path
            Path basePath = Paths.get(reportsBasePath).getParent(); // Get ./storage
            Path filePath = basePath.resolve(reportUrl);

            log.info("Retrieving report from: {}", filePath);
            return Files.readString(filePath);
        } catch (IOException e) {
            log.error("Failed to retrieve report: {}", reportUrl, e);
            throw new RuntimeException("Failed to retrieve report", e);
        }
    }
}