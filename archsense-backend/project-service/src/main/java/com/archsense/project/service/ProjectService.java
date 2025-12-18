package com.archsense.project.service;

import com.archsense.common.dto.request.CreateProjectRequest;
import com.archsense.common.dto.request.UpdateProjectRequest;
import com.archsense.common.dto.response.ProjectResponse;
import com.archsense.common.event.ProjectDeletedEvent;
import com.archsense.common.exception.ResourceNotFoundException;
import com.archsense.project.domain.Project;
import com.archsense.project.messaging.ProjectEventPublisher;
import com.archsense.project.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectEventPublisher eventPublisher;

    public ProjectService(ProjectRepository projectRepository, ProjectEventPublisher eventPublisher) {
        this.projectRepository = projectRepository;
        this.eventPublisher = eventPublisher;
    }

    public ProjectResponse create(String userId, CreateProjectRequest request) {
        Project project = new Project(userId, request.name(), request.description());

        // NEW: Set constraints if provided
        if (request.constraints() != null) {
            Project.ProjectConstraints domainConstraints = new Project.ProjectConstraints();
            domainConstraints.setExpectedQps(request.constraints().expectedQps());
            domainConstraints.setLatencyTargetMs(request.constraints().latencyTargetMs());
            domainConstraints.setConsistencyLevel(request.constraints().consistencyLevel());
            domainConstraints.setBudgetSensitivity(request.constraints().budgetSensitivity());
            project.setConstraints(domainConstraints);
        }

        project = projectRepository.save(project);
        return toResponse(project);
    }

    public List<ProjectResponse> listByUser(String userId) {
        return projectRepository.findByUserIdAndDeletedFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public ProjectResponse getById(String projectId, String userId) {
        Project project = projectRepository.findByIdAndUserIdAndDeletedFalse(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
        return toResponse(project);
    }

    public ProjectResponse update(String projectId, String userId, UpdateProjectRequest request) {
        Project project = projectRepository.findByIdAndUserIdAndDeletedFalse(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        // NEW: Update constraints if provided
        if (request.constraints() != null) {
            Project.ProjectConstraints domainConstraints = new Project.ProjectConstraints();
            domainConstraints.setExpectedQps(request.constraints().expectedQps());
            domainConstraints.setLatencyTargetMs(request.constraints().latencyTargetMs());
            domainConstraints.setConsistencyLevel(request.constraints().consistencyLevel());
            domainConstraints.setBudgetSensitivity(request.constraints().budgetSensitivity());
            project.setConstraints(domainConstraints);
        }

        project = projectRepository.save(project);

        return toResponse(project);
    }

    public void delete(String projectId, String userId) {
        Project project = projectRepository.findByIdAndUserIdAndDeletedFalse(projectId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found"));

        project.markDeleted();
        projectRepository.save(project);

        ProjectDeletedEvent event = new ProjectDeletedEvent(
                project.getId(),
                project.getUserId(),
                Instant.now()
        );
        eventPublisher.publishProjectDeleted(event);
    }

    private ProjectResponse toResponse(Project project) {
        com.archsense.common.dto.ProjectConstraints dtoConstraints = null;
        if (project.getConstraints() != null) {
            dtoConstraints = new com.archsense.common.dto.ProjectConstraints(
                    project.getConstraints().getExpectedQps(),
                    project.getConstraints().getLatencyTargetMs(),
                    project.getConstraints().getConsistencyLevel(),
                    project.getConstraints().getBudgetSensitivity()
            );
        }

        return new ProjectResponse(
                project.getId(),
                project.getUserId(),
                project.getName(),
                project.getDescription(),
                project.getCreatedAt(),
                project.getUpdatedAt(),
                dtoConstraints
        );
    }
}