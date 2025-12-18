    package com.archsense.common.event;

    import java.time.Instant;

    public record ProjectDeletedEvent(
            String projectId,
            String userId,
            Instant deletedAt
    ) {
    }