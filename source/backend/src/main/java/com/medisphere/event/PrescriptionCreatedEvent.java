package com.medisphere.event;

import java.util.UUID;

public record PrescriptionCreatedEvent(UUID patientId, String doctorName, int itemCount) implements DomainEvent {
    @Override
    public String summary() {
        return "Prescription created by Dr. " + doctorName + " (" + itemCount + " item(s))";
    }
}
