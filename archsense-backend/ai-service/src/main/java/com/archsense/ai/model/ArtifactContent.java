package com.archsense.ai.model;

public class ArtifactContent {
    private final String artifactId;
    private final String name;
    private final String type;
    private final byte[] content;

    public ArtifactContent(String artifactId, String name, String type, byte[] content) {
        this.artifactId = artifactId;
        this.name = name;
        this.type = type;
        this.content = content;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public byte[] getContent() {
        return content;
    }

    public int getContentSize() {
        return content != null ? content.length : 0;
    }
}