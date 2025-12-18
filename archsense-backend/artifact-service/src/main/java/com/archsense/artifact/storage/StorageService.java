package com.archsense.artifact.storage;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface StorageService {

    String store(String key, MultipartFile file);

    InputStream retrieve(String key);

    void delete(String key);
}