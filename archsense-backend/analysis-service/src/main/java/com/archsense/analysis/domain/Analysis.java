package com.archsense.analysis.domain;

import com.archsense.common.constant.AnalysisStatus;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Document(collection = "analyses")
@CompoundIndex(name = "projectId_userId", def = "{'projectId': 1, 'userId': 1}")
public class Analysis {

    @Id
    private String id;

    private String projectId;
    private String userId;
    private List<String> artifactIds;
    private AnalysisStatus status;
    private String reportS3Key;
    private Instant createdAt;
    private Instant completedAt;
    private String error;

    public Analysis() {
    }

    public Analysis(String projectId, String userId, List<String> artifactIds) {
        this.projectId = projectId;
        this.userId = userId;
        this.artifactIds = artifactIds;
        this.status = AnalysisStatus.PENDING;
        this.createdAt = Instant.now();
    }

    public void markInProgress() {
        this.status = AnalysisStatus.IN_PROGRESS;
    }

    public void markCompleted(String reportS3Key) {
        this.status = AnalysisStatus.COMPLETED;
        this.reportS3Key = reportS3Key;
        this.completedAt = Instant.now();
    }

    public void markFailed(String error) {
        this.status = AnalysisStatus.FAILED;
        this.error = error;
        this.completedAt = Instant.now();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
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

    public String getReportS3Key() {
        return reportS3Key;
    }

    public void setReportS3Key(String reportS3Key) {
        this.reportS3Key = reportS3Key;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}