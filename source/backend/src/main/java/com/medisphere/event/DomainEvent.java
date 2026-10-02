package com.medisphere.event;

import java.util.UUID;

/**
 * Marker for every domain event MediSphere publishes through Spring's
 * {@code ApplicationEventPublisher}. Keeping these as small records (rather than
 * a generic "type + payload map" event) gives listeners compile-time safety and
 * keeps the operations timeline and notification engine self-documenting.
 */
public sealed interface DomainEvent
        permits PatientRegisteredEvent, AppointmentCreatedEvent, PatientArrivedEvent,
        EmergencyPatientArrivedEvent, DoctorStartedConsultationEvent, LabResultCompletedEvent,
        BedReleasedEvent, PrescriptionCreatedEvent, PaymentCompletedEvent, DischargeCompletedEvent {

    /** The patient this event is about, if any (some, like BedReleasedEvent, may have none). */
    UUID patientId();

    /** Human-readable line for the operations timeline and notifications. */
    String summary();
}
