package com.archsense.artifact.messaging;

import com.archsense.artifact.service.ArtifactService;
import com.archsense.common.constant.KafkaTopics;
import com.archsense.common.event.ProjectDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class ProjectEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(ProjectEventConsumer.class);

    private final ArtifactService artifactService;

    public ProjectEventConsumer(ArtifactService artifactService) {
        this.artifactService = artifactService;
    }

    @KafkaListener(topics = KafkaTopics.PROJECT_DELETED, groupId = "artifact-service")
    public void handleProjectDeleted(ProjectDeletedEvent event) {
        log.info("Received ProjectDeletedEvent for projectId: {}", event.projectId());
        artifactService.deleteByProject(event.projectId());
        log.info("Deleted all artifacts for projectId: {}", event.projectId());
    }
}