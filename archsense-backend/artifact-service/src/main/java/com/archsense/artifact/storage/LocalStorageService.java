package com.archsense.artifact.storage;

import com.archsense.common.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
@ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
public class LocalStorageService implements StorageService {

    private final Path basePath;

    public LocalStorageService(@Value("${storage.local.base-path}") String basePath) {
        this.basePath = Paths.get(basePath);
        try {
            Files.createDirectories(this.basePath);
        } catch (IOException e) {
            throw new StorageException("Failed to initialize storage directory", e);
        }
    }

    @Override
    public String store(String key, MultipartFile file) {
        try {
            Path targetPath = basePath.resolve(key);
            Files.createDirectories(targetPath.getParent());

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetPath, StandardCopyOption.REPLACE_EXISTING);
            }

            return key;
        } catch (IOException e) {
            throw new StorageException("Failed to store file: " + key, e);
        }
    }

    @Override
    public InputStream retrieve(String key) {
        try {
            Path filePath = basePath.resolve(key);
            if (!Files.exists(filePath)) {
                throw new StorageException("File not found: " + key);
            }
            return Files.newInputStream(filePath);
        } catch (IOException e) {
            throw new StorageException("Failed to retrieve file: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Path filePath = basePath.resolve(key);
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            throw new StorageException("Failed to delete file: " + key, e);
        }
    }
}