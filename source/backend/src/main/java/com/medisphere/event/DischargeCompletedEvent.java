package com.medisphere.event;

import java.util.UUID;

public record DischargeCompletedEvent(UUID patientId) implements DomainEvent {
    @Override
    public String summary() {
        return "Discharge completed";
    }
}
