package com.archsense.artifact.controller;

import com.archsense.artifact.domain.Artifact;
import com.archsense.artifact.repository.ArtifactRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/internal/artifacts")
public class InternalArtifactController {

    private static final Logger log = LoggerFactory.getLogger(InternalArtifactController.class);

    private final ArtifactRepository artifactRepository;

    public InternalArtifactController(ArtifactRepository artifactRepository) {
        this.artifactRepository = artifactRepository;
    }

    @GetMapping("/batch")
    public ResponseEntity<List<Artifact>> getArtifactsBatch(
            @RequestParam("ids") List<String> ids,
            @RequestHeader("X-User-Id") String userId) {

        log.info("Fetching batch of {} artifacts for user: {}", ids.size(), userId);

        List<Artifact> artifacts = artifactRepository.findAllById(ids);

        // Filter by userId to ensure security
        List<Artifact> userArtifacts = artifacts.stream()
                .filter(a -> a.getUserId().equals(userId))
                .toList();

        log.info("Returning {} artifacts", userArtifacts.size());
        return ResponseEntity.ok(userArtifacts);
    }
}