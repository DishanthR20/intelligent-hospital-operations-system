package com.medisphere.event;

import java.util.UUID;

public record DoctorStartedConsultationEvent(UUID patientId, String doctorName) implements DomainEvent {
    @Override
    public String summary() {
        return "Consultation started with Dr. " + doctorName;
    }
}
