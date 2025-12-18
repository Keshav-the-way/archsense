package com.archsense.analysis.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "analyses")
public class Analysis {

    @Id
    private String id;
    private String userId;
    private String projectId;
    private List<String> artifactIds;
    private AnalysisStatus status;
    private String reportUrl;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant completedAt;
    private Integer version;                    // Analysis version number
    private String previousAnalysisId;          // Link to previous version
    private EvolutionMetrics evolutionMetrics; // Comparison with previous version

    public static class EvolutionMetrics {
        private Integer issuesResolved;
        private Integer newIssues;
        private Integer issuesRegressed;
        private Double improvementScore;  // 0-100 score

        public EvolutionMetrics() {}

        public Integer getIssuesResolved() { return issuesResolved; }
        public void setIssuesResolved(Integer issuesResolved) { this.issuesResolved = issuesResolved; }

        public Integer getNewIssues() { return newIssues; }
        public void setNewIssues(Integer newIssues) { this.newIssues = newIssues; }

        public Integer getIssuesRegressed() { return issuesRegressed; }
        public void setIssuesRegressed(Integer issuesRegressed) { this.issuesRegressed = issuesRegressed; }

        public Double getImprovementScore() { return improvementScore; }
        public void setImprovementScore(Double improvementScore) { this.improvementScore = improvementScore; }
    }

    public Integer getVersion() { return version; }
    public void setVersion(Integer version) { this.version = version; }

    public String getPreviousAnalysisId() { return previousAnalysisId; }
    public void setPreviousAnalysisId(String previousAnalysisId) { this.previousAnalysisId = previousAnalysisId; }

    public EvolutionMetrics getEvolutionMetrics() { return evolutionMetrics; }
    public void setEvolutionMetrics(EvolutionMetrics evolutionMetrics) { this.evolutionMetrics = evolutionMetrics; }

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

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public List<String> getArtifactIds() {
        return artifactIds;
    }

    public void setArtifactIds(List<String> artifactIds) {
        this.artifactIds = artifactIds;
    }

    public AnalysisStatus getStatus() {
        return status;
    }

    public void setStatus(AnalysisStatus status) {
        this.status = status;
    }

    public String getReportUrl() {
        return reportUrl;
    }

    public void setReportUrl(String reportUrl) {
        this.reportUrl = reportUrl;
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

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}