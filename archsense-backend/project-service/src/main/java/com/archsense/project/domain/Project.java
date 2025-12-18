package com.archsense.project.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "projects")
@CompoundIndex(name = "userId_deleted", def = "{'userId': 1, 'deleted': 1}")
public class Project {

    @Id
    private String id;

    private String userId;
    private String name;
    private String description;
    private boolean deleted;
    private Instant createdAt;
    private Instant updatedAt;
    private ProjectConstraints constraints;

    public static class ProjectConstraints {
        private Integer expectedQps;
        private Integer latencyTargetMs;
        private String consistencyLevel;  // "EVENTUAL", "STRONG", "STRICT"
        private String budgetSensitivity;  // "LOW", "MEDIUM", "HIGH"

        public ProjectConstraints() {}

        // Getters and Setters
        public Integer getExpectedQps() { return expectedQps; }
        public void setExpectedQps(Integer expectedQps) { this.expectedQps = expectedQps; }

        public Integer getLatencyTargetMs() { return latencyTargetMs; }
        public void setLatencyTargetMs(Integer latencyTargetMs) { this.latencyTargetMs = latencyTargetMs; }

        public String getConsistencyLevel() { return consistencyLevel; }
        public void setConsistencyLevel(String consistencyLevel) { this.consistencyLevel = consistencyLevel; }

        public String getBudgetSensitivity() { return budgetSensitivity; }
        public void setBudgetSensitivity(String budgetSensitivity) { this.budgetSensitivity = budgetSensitivity; }
    }

//    public enum ConsistencyLevel {
//        EVENTUAL, STRONG, STRICT
//    }
//
//    public enum BudgetSensitivity {
//        LOW, MEDIUM, HIGH
//    }

    public ProjectConstraints getConstraints() {
        return constraints;
    }

    public void setConstraints(ProjectConstraints constraints) {
        this.constraints = constraints;
    }

    public Project() {
    }

    public Project(String userId, String name, String description) {
        this.userId = userId;
        this.name = name;
        this.description = description;
        this.deleted = false;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void update(String name, String description) {
        if (name != null && !name.isBlank()) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
        this.updatedAt = Instant.now();
    }

    public void markDeleted() {
        this.deleted = true;
        this.updatedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}