package com.archsense.artifact.service;

import com.archsense.artifact.domain.Artifact;
import com.archsense.artifact.messaging.ArtifactEventPublisher;
import com.archsense.artifact.repository.ArtifactRepository;
import com.archsense.artifact.storage.StorageService;
import com.archsense.common.constant.ArtifactType;
import com.archsense.common.dto.response.ArtifactResponse;
import com.archsense.common.event.ArtifactDeletedEvent;
import com.archsense.common.event.ArtifactUploadedEvent;
import com.archsense.common.exception.ResourceNotFoundException;
import com.archsense.common.exception.ValidationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class ArtifactService {

    private final ArtifactRepository artifactRepository;
    private final StorageService storageService;
    private final ArtifactEventPublisher eventPublisher;

    public ArtifactService(ArtifactRepository artifactRepository,
                           StorageService storageService,
                           ArtifactEventPublisher eventPublisher) {
        this.artifactRepository = artifactRepository;
        this.storageService = storageService;
        this.eventPublisher = eventPublisher;
    }

    public ArtifactResponse upload(String projectId, String userId, MultipartFile file) {
        if (file.isEmpty()) {
            throw new ValidationException("File cannot be empty");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new ValidationException("Invalid filename");
        }

        ArtifactType type = determineType(originalFilename);
        String artifactId = UUID.randomUUID().toString();
        String s3Key = buildS3Key(userId, projectId, artifactId, originalFilename);

        storageService.store(s3Key, file);

        Artifact artifact = new Artifact(projectId, userId, originalFilename, type, s3Key, file.getSize());
        artifact = artifactRepository.save(artifact);

        ArtifactUploadedEvent event = new ArtifactUploadedEvent(
                artifact.getId(),
                artifact.getProjectId(),
                artifact.getUserId(),
                artifact.getName(),
                artifact.getType(),
                artifact.getS3Key(),
                artifact.getSize(),
                artifact.getUploadedAt()
        );
        eventPublisher.publishArtifactUploaded(event);

        return toResponse(artifact);
    }

    public List<ArtifactResponse> listByProject(String projectId, String userId) {
        return artifactRepository.findByProjectIdAndUserIdOrderByUploadedAtDesc(projectId, userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ArtifactResponse getById(String artifactId, String userId) {
        Artifact artifact = artifactRepository.findByIdAndUserId(artifactId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Artifact not found"));
        return toResponse(artifact);
    }

    public InputStream download(String artifactId, String userId) {
        Artifact artifact = artifactRepository.findByIdAndUserId(artifactId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Artifact not found"));
        return storageService.retrieve(artifact.getS3Key());
    }

    public void delete(String artifactId, String userId) {
        Artifact artifact = artifactRepository.findByIdAndUserId(artifactId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Artifact not found"));

        storageService.delete(artifact.getS3Key());
        artifactRepository.deleteById(artifactId);

        ArtifactDeletedEvent event = new ArtifactDeletedEvent(
                artifact.getId(),
                artifact.getProjectId(),
                artifact.getUserId(),
                artifact.getS3Key(),
                Instant.now()
        );
        eventPublisher.publishArtifactDeleted(event);
    }

    public void deleteByProject(String projectId) {
        List<Artifact> artifacts = artifactRepository.findByProjectId(projectId);
        for (Artifact artifact : artifacts) {
            storageService.delete(artifact.getS3Key());
            artifactRepository.deleteById(artifact.getId());
        }
    }

    private ArtifactType determineType(String filename) {
        String lowerName = filename.toLowerCase();
        if (lowerName.endsWith(".png") || lowerName.endsWith(".jpg") || lowerName.endsWith(".jpeg") || lowerName.endsWith(".gif")) {
            return ArtifactType.IMAGE;
        } else if (lowerName.endsWith(".pdf")) {
            return ArtifactType.PDF;
        } else if (lowerName.endsWith(".md")) {
            return ArtifactType.MARKDOWN;
        } else if (lowerName.endsWith(".txt")) {
            return ArtifactType.TEXT;
        }
        return ArtifactType.OTHER;
    }

    private String buildS3Key(String userId, String projectId, String artifactId, String filename) {
        return String.format("artifacts/%s/%s/%s/%s", userId, projectId, artifactId, filename);
    }

    private ArtifactResponse toResponse(Artifact artifact) {
        return new ArtifactResponse(
                artifact.getId(),
                artifact.getProjectId(),
                artifact.getUserId(),
                artifact.getName(),
                artifact.getType(),
                artifact.getSize(),
                artifact.getS3Key(),
                artifact.getUploadedAt()
        );
    }
}