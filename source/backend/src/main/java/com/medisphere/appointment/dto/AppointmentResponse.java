package com.medisphere.appointment.dto;

import com.medisphere.appointment.Appointment;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id, UUID patientId, String patientName, UUID doctorId, String doctorName,
        String departmentName, LocalDateTime scheduledAt, int durationMinutes,
        String status, String reason, String tokenNumber
) {
    public static AppointmentResponse from(Appointment a) {
        return new AppointmentResponse(
                a.getId(), a.getPatient().getId(), a.getPatient().getUser().getFullName(),
                a.getDoctor().getId(), a.getDoctor().getUser().getFullName(),
                a.getDoctor().getDepartment().getName(), a.getScheduledAt(), a.getDurationMinutes(),
                a.getStatus().name(), a.getReason(), a.getTokenNumber());
    }
}
