package com.medisphere.event;

import java.util.UUID;

public record PatientRegisteredEvent(UUID patientId, String patientName) implements DomainEvent {
    @Override
    public String summary() {
        return "Patient registered: " + patientName;
    }
}
