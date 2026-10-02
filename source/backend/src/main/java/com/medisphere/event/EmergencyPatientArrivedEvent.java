package com.medisphere.event;

import java.util.UUID;

public record EmergencyPatientArrivedEvent(UUID patientId, String severity) implements DomainEvent {
    @Override
    public String summary() {
        return "EMERGENCY arrival, severity " + severity;
    }
}
