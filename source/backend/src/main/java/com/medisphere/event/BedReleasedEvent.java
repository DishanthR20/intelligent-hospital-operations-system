package com.medisphere.event;

import java.util.UUID;

public record BedReleasedEvent(UUID bedId, String bedNumber, String wardName) implements DomainEvent {
    @Override
    public UUID patientId() {
        return null;
    }

    @Override
    public String summary() {
        return "Bed " + bedNumber + " released in " + wardName;
    }
}
