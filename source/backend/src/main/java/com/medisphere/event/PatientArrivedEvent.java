package com.medisphere.event;

import java.util.UUID;

public record PatientArrivedEvent(UUID patientId, UUID departmentId, String departmentName,
                                   String tokenNumber) implements DomainEvent {
    @Override
    public String summary() {
        return "Checked in at " + departmentName + ", token " + tokenNumber;
    }
}
