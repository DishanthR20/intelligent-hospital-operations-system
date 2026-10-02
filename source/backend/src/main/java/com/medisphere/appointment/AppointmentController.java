package com.medisphere.appointment;

import com.medisphere.appointment.dto.AppointmentResponse;
import com.medisphere.appointment.dto.BookAppointmentRequest;
import com.medisphere.appointment.dto.SlotSuggestion;
import com.medisphere.patient.PatientService;
import com.medisphere.security.CurrentUser;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;
    private final PatientService patientService;

    public AppointmentController(AppointmentService appointmentService, PatientService patientService) {
        this.appointmentService = appointmentService;
        this.patientService = patientService;
    }

    @GetMapping("/suggested-slots")
    public List<SlotSuggestion> suggestedSlots(@RequestParam UUID doctorId,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return appointmentService.suggestSlots(doctorId, date);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    public AppointmentResponse book(@Valid @RequestBody BookAppointmentRequest req) {
        UUID patientId = req.patientId() != null ? req.patientId()
                : patientService.getByUserId(CurrentUser.get().getId()).getId();
        return appointmentService.book(patientId, req.doctorId(), req.scheduledAt(), req.reason());
    }

    @PostMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    public AppointmentResponse reschedule(@PathVariable UUID id, @RequestBody Map<String, LocalDateTime> body) {
        return appointmentService.reschedule(id, body.get("scheduledAt"));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','PATIENT')")
    public void cancel(@PathVariable UUID id) {
        appointmentService.cancel(id);
    }

    @PostMapping("/{id}/no-show")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public void noShow(@PathVariable UUID id) {
        appointmentService.markNoShow(id);
    }

    @PostMapping("/{id}/check-in")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public AppointmentResponse checkIn(@PathVariable UUID id) {
        return appointmentService.checkIn(id);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST','PATIENT')")
    public List<AppointmentResponse> forPatient(@PathVariable UUID patientId) {
        return appointmentService.forPatient(patientId);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<AppointmentResponse> mine() {
        return appointmentService.forPatient(patientService.getByUserId(CurrentUser.get().getId()).getId());
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    public List<AppointmentResponse> forDoctor(@PathVariable UUID doctorId) {
        return appointmentService.forDoctor(doctorId);
    }
}
