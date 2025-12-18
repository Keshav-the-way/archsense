package com.archsense.executor.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.nio.charset.StandardCharsets;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageService(
            S3Client s3Client,
            @Value("${storage.s3.bucket-reports}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        log.info("S3StorageService initialized with bucket: {}", bucketName);
    }

    public String storeReport(String userId, String projectId, String fileName, String content) {
        String key = String.format("reports/%s/%s/%s", userId, projectId, fileName);

        log.info("Storing report in S3: bucket={}, key={}", bucketName, key);

        try {
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType("application/json")
                    .build();

            s3Client.putObject(putRequest, RequestBody.fromString(content, StandardCharsets.UTF_8));

            log.info("Successfully stored report in S3: {}", key);
            return key;
        } catch (S3Exception e) {
            log.error("Failed to store report in S3", e);
            throw new RuntimeException("Failed to store report in S3", e);
        }
    }

    public String retrieveReport(String reportUrl) {
        log.info("Retrieving report from S3: bucket={}, key={}", bucketName, reportUrl);

        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(reportUrl)
                    .build();

            return s3Client.getObjectAsBytes(getRequest).asUtf8String();
        } catch (S3Exception e) {
            log.error("Failed to retrieve report from S3", e);
            throw new RuntimeException("Failed to retrieve report from S3", e);
        }
    }
}