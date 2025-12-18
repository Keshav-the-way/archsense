package com.archsense.artifact.domain;

import com.archsense.common.constant.ArtifactType;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "artifacts")
@CompoundIndex(name = "projectId_userId", def = "{'projectId': 1, 'userId': 1}")
public class Artifact {

    @Id
    private String id;

    private String projectId;
    private String userId;
    private String name;
    private ArtifactType type;
    private String s3Key;
    private long size;
    private Instant uploadedAt;

    public Artifact() {
    }

    public Artifact(String projectId, String userId, String name, ArtifactType type, String s3Key, long size) {
        this.projectId = projectId;
        this.userId = userId;
        this.name = name;
        this.type = type;
        this.s3Key = s3Key;
        this.size = size;
        this.uploadedAt = Instant.now();
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArtifactType getType() {
        return type;
    }

    public void setType(ArtifactType type) {
        this.type = type;
    }

    public String getS3Key() {
        return s3Key;
    }

    public void setS3Key(String s3Key) {
        this.s3Key = s3Key;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public Instant getUploadedAt() {
        return uploadedAt;
    }

    public void setUploadedAt(Instant uploadedAt) {
        this.uploadedAt = uploadedAt;
    }
}