package com.archsense.project.messaging;

import com.archsense.common.constant.KafkaTopics;
import com.archsense.common.event.ProjectDeletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ProjectEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ProjectEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProjectEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishProjectDeleted(ProjectDeletedEvent event) {
        log.info("Publishing ProjectDeletedEvent for projectId: {}", event.projectId());
        kafkaTemplate.send(KafkaTopics.PROJECT_DELETED, event.projectId(), event);
    }
}