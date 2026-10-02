package com.medisphere.appointment;

import com.medisphere.appointment.dto.AppointmentResponse;
import com.medisphere.appointment.dto.SlotSuggestion;
import com.medisphere.common.exception.ConflictException;
import com.medisphere.common.exception.ResourceNotFoundException;
import com.medisphere.doctor.Doctor;
import com.medisphere.doctor.DoctorService;
import com.medisphere.event.AppointmentCreatedEvent;
import com.medisphere.patient.Patient;
import com.medisphere.patient.PatientService;
import com.medisphere.queue.QueueService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final SlotOptimizer slotOptimizer;
    private final QueueService queueService;
    private final ApplicationEventPublisher eventPublisher;

    public AppointmentService(AppointmentRepository appointmentRepository, PatientService patientService,
                               DoctorService doctorService, SlotOptimizer slotOptimizer,
                               QueueService queueService, ApplicationEventPublisher eventPublisher) {
        this.appointmentRepository = appointmentRepository;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.slotOptimizer = slotOptimizer;
        this.queueService = queueService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public List<SlotSuggestion> suggestSlots(UUID doctorId, LocalDate date) {
        return slotOptimizer.suggest(doctorService.getEntity(doctorId), date);
    }

    public AppointmentResponse book(UUID patientId, UUID doctorId, LocalDateTime scheduledAt, String reason) {
        Patient patient = patientService.getEntity(patientId);
        Doctor doctor = doctorService.getEntity(doctorId);
        int duration = doctor.getConsultationMinutes();

        List<Appointment> overlapping = appointmentRepository.findOverlapping(
                doctorId, scheduledAt, scheduledAt.plusMinutes(duration));
        if (!overlapping.isEmpty()) {
            throw new ConflictException("SLOT_UNAVAILABLE",
                    "Dr. " + doctor.getUser().getFullName() + " already has an appointment at that time");
        }

        Appointment appointment = new Appointment(patient, doctor, scheduledAt, duration, reason);
        appointmentRepository.save(appointment);

        eventPublisher.publishEvent(new AppointmentCreatedEvent(patient.getId(), appointment.getId(),
                doctor.getUser().getFullName(), scheduledAt));
        return AppointmentResponse.from(appointment);
    }

    public AppointmentResponse reschedule(UUID appointmentId, LocalDateTime newTime) {
        Appointment appointment = getEntity(appointmentId);
        List<Appointment> overlapping = appointmentRepository.findOverlapping(
                appointment.getDoctor().getId(), newTime, newTime.plusMinutes(appointment.getDurationMinutes()));
        overlapping.removeIf(a -> a.getId().equals(appointmentId));
        if (!overlapping.isEmpty()) {
            throw new ConflictException("SLOT_UNAVAILABLE", "That doctor already has an appointment at the new time");
        }
        appointment.setScheduledAt(newTime);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return AppointmentResponse.from(appointment);
    }

    public void cancel(UUID appointmentId) {
        getEntity(appointmentId).setStatus(AppointmentStatus.CANCELLED);
    }

    public void markNoShow(UUID appointmentId) {
        getEntity(appointmentId).setStatus(AppointmentStatus.NO_SHOW);
    }

    /** Front-desk check-in: moves the appointment to CHECKED_IN and drops the patient into the department queue. */
    public AppointmentResponse checkIn(UUID appointmentId) {
        Appointment appointment = getEntity(appointmentId);
        appointment.setStatus(AppointmentStatus.WAITING);
        var queueEntry = queueService.addToQueue(appointment.getPatient().getId(),
                appointment.getDoctor().getDepartment().getId(), false, 0);
        queueService.assignDoctor(queueEntry.getId(), appointment.getDoctor().getId());
        appointment.setTokenNumber(queueEntry.getTokenNumber());
        return AppointmentResponse.from(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> forPatient(UUID patientId) {
        return appointmentRepository.findByPatient_IdOrderByScheduledAtDesc(patientId).stream()
                .map(AppointmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentResponse> forDoctor(UUID doctorId) {
        return appointmentRepository.findByDoctor_IdOrderByScheduledAtDesc(doctorId).stream()
                .map(AppointmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Appointment getEntity(UUID id) {
        return appointmentRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Appointment", id));
    }
}
