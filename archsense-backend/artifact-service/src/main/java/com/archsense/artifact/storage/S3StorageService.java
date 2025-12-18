package com.archsense.artifact.storage;

import com.archsense.common.exception.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.io.InputStream;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "s3")
public class S3StorageService implements StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    private final S3Client s3Client;
    private final String bucketName;

    public S3StorageService(
            S3Client s3Client,
            @Value("${storage.s3.bucket-artifacts}") String bucketName) {
        this.s3Client = s3Client;
        this.bucketName = bucketName;
        log.info("S3StorageService initialized with bucket: {}", bucketName);
    }

    @Override
    public String store(String key, MultipartFile file) {
        try {
            log.info("Storing file in S3: bucket={}, key={}, size={}", bucketName, key, file.getSize());

            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Successfully stored file in S3: {}", key);
            return key;
        } catch (IOException e) {
            throw new StorageException("Failed to store file in S3: " + key, e);
        } catch (S3Exception e) {
            throw new StorageException("S3 error while storing file: " + key, e);
        }
    }

    @Override
    public InputStream retrieve(String key) {
        try {
            log.info("Retrieving file from S3: bucket={}, key={}", bucketName, key);

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            return s3Client.getObject(getObjectRequest);
        } catch (S3Exception e) {
            throw new StorageException("Failed to retrieve file from S3: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            log.info("Deleting file from S3: bucket={}, key={}", bucketName, key);

            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);

            log.info("Successfully deleted file from S3: {}", key);
        } catch (S3Exception e) {
            throw new StorageException("Failed to delete file from S3: " + key, e);
        }
    }
}