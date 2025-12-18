package com.archsense.project.repository;

import com.archsense.project.domain.Project;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends MongoRepository<Project, String> {

    List<Project> findByUserIdAndDeletedFalseOrderByCreatedAtDesc(String userId);

    Optional<Project> findByIdAndUserId(String id, String userId);

    Optional<Project> findByIdAndUserIdAndDeletedFalse(String id, String userId);
}