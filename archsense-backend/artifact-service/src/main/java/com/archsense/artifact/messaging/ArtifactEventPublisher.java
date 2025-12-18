package com.archsense.artifact.messaging;

import com.archsense.common.constant.KafkaTopics;
import com.archsense.common.event.ArtifactDeletedEvent;
import com.archsense.common.event.ArtifactUploadedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ArtifactEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ArtifactEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ArtifactEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishArtifactUploaded(ArtifactUploadedEvent event) {
        log.info("Publishing ArtifactUploadedEvent for artifactId: {}", event.artifactId());
        kafkaTemplate.send(KafkaTopics.ARTIFACT_UPLOADED, event.artifactId(), event);
    }

    public void publishArtifactDeleted(ArtifactDeletedEvent event) {
        log.info("Publishing ArtifactDeletedEvent for artifactId: {}", event.artifactId());
        kafkaTemplate.send(KafkaTopics.ARTIFACT_DELETED, event.artifactId(), event);
    }
}