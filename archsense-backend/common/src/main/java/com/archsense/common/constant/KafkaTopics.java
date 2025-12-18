package com.archsense.common.constant;

public class KafkaTopics {
    public static final String PROJECT_DELETED = "project.deleted";
    public static final String ARTIFACT_UPLOADED = "artifact.uploaded";
    public static final String ARTIFACT_DELETED = "artifact.deleted";
    public static final String ANALYSIS_REQUESTED = "analysis.requested";
    public static final String ANALYSIS_COMPLETED = "analysis.completed";
    public static final String ANALYSIS_FAILED = "analysis.failed";

    private KafkaTopics() {
    }
}