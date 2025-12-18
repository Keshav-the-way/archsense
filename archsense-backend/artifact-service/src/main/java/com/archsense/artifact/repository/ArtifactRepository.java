package com.archsense.artifact.repository;

import com.archsense.artifact.domain.Artifact;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArtifactRepository extends MongoRepository<Artifact, String> {

    List<Artifact> findByProjectIdAndUserIdOrderByUploadedAtDesc(String projectId, String userId);

    Optional<Artifact> findByIdAndUserId(String id, String userId);

    List<Artifact> findByProjectId(String projectId);
}