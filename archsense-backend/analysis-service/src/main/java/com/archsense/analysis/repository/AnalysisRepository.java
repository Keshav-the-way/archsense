package com.archsense.analysis.repository;

import com.archsense.analysis.model.Analysis;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisRepository extends MongoRepository<Analysis, String> {
    List<Analysis> findByUserIdAndProjectIdOrderByCreatedAtDesc(String userId, String projectId);
    Optional<Analysis> findByIdAndUserId(String id, String userId);
}