package com.archsense.artifact.controller;

import com.archsense.artifact.service.ArtifactService;
import com.archsense.common.dto.response.ArtifactResponse;
import com.archsense.common.dto.response.ErrorResponse;
import com.archsense.common.exception.ResourceNotFoundException;
import com.archsense.common.exception.StorageException;
import com.archsense.common.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ArtifactController {

    private final ArtifactService artifactService;

    public ArtifactController(ArtifactService artifactService) {
        this.artifactService = artifactService;
    }

    @PostMapping("/projects/{projectId}/artifacts")
    public ResponseEntity<ArtifactResponse> upload(
            @PathVariable String projectId,
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("file") MultipartFile file) {
        ArtifactResponse response = artifactService.upload(projectId, userId, file);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/projects/{projectId}/artifacts")
    public ResponseEntity<List<ArtifactResponse>> list(
            @PathVariable String projectId,
            @RequestHeader("X-User-Id") String userId) {
        List<ArtifactResponse> response = artifactService.listByProject(projectId, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/artifacts/{id}")
    public ResponseEntity<ArtifactResponse> getById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        ArtifactResponse response = artifactService.getById(id, userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/artifacts/{id}/download")
    public ResponseEntity<InputStreamResource> download(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        InputStream inputStream = artifactService.download(id, userId);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @DeleteMapping("/artifacts/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {
        artifactService.delete(id, userId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.NOT_FOUND.value(),
                "Not Found",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(StorageException.class)
    public ResponseEntity<ErrorResponse> handleStorageException(
            StorageException ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Storage Error",
                ex.getMessage(),
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception ex, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "An unexpected error occurred",
                request.getRequestURI()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}