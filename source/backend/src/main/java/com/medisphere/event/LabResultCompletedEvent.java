package com.medisphere.event;

import java.util.UUID;

public record LabResultCompletedEvent(UUID patientId, String testName, boolean critical) implements DomainEvent {
    @Override
    public String summary() {
        return (critical ? "CRITICAL result for " : "Result ready for ") + testName;
    }
}
