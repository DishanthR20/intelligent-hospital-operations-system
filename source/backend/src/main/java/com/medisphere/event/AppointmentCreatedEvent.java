package com.medisphere.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentCreatedEvent(UUID patientId, UUID appointmentId, String doctorName,
                                       LocalDateTime scheduledAt) implements DomainEvent {
    @Override
    public String summary() {
        return "Appointment booked with Dr. " + doctorName + " at " + scheduledAt;
    }
}
