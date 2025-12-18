package com.archsense.common.dto;

public record ProjectConstraints(
        Integer expectedQps,
        Integer latencyTargetMs,
        String consistencyLevel,  // "EVENTUAL", "STRONG", "STRICT"
        String budgetSensitivity   // "LOW", "MEDIUM", "HIGH"
) {
    public ProjectConstraints() {
        this(null, null, null, null);
    }
}