package com.medisphere.consultation;

import com.medisphere.appointment.Appointment;
import com.medisphere.appointment.AppointmentService;
import com.medisphere.appointment.AppointmentStatus;
import com.medisphere.audit.AuditAction;
import com.medisphere.audit.AuditService;
import com.medisphere.common.exception.ResourceNotFoundException;
import com.medisphere.consultation.dto.ConsultationResponse;
import com.medisphere.consultation.dto.CreateConsultationRequest;
import com.medisphere.doctor.Doctor;
import com.medisphere.doctor.DoctorService;
import com.medisphere.event.DoctorStartedConsultationEvent;
import com.medisphere.patient.Patient;
import com.medisphere.patient.PatientService;
import com.medisphere.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ConsultationService {

    private final ConsultationRepository repository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;

    public ConsultationService(ConsultationRepository repository, PatientService patientService,
                                DoctorService doctorService, AppointmentService appointmentService,
                                ApplicationEventPublisher eventPublisher, AuditService auditService) {
        this.repository = repository;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
    }

    public ConsultationResponse create(CreateConsultationRequest req) {
        Patient patient = patientService.getEntity(req.patientId());
        Doctor doctor = doctorService.getByUserId(CurrentUser.get().getId());
        Appointment appointment = req.appointmentId() != null ? appointmentService.getEntity(req.appointmentId()) : null;

        eventPublisher.publishEvent(new DoctorStartedConsultationEvent(patient.getId(), doctor.getUser().getFullName()));
        Consultation consultation = new Consultation(patient, doctor, appointment, req.notes(), req.diagnosis(), req.followUpPlan());
        repository.save(consultation);

        if (appointment != null) {
            appointment.setStatus(AppointmentStatus.COMPLETED);
        }
        auditService.record(AuditAction.VIEW_MEDICAL_RECORD, "Consultation", consultation.getId().toString());
        return ConsultationResponse.from(consultation);
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> forPatient(UUID patientId) {
        auditService.record(AuditAction.VIEW_MEDICAL_RECORD, "Patient", patientId.toString());
        return repository.findByPatient_IdOrderByCreatedAtDesc(patientId).stream().map(ConsultationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<ConsultationResponse> forDoctor(UUID doctorId) {
        return repository.findByDoctor_IdOrderByCreatedAtDesc(doctorId).stream().map(ConsultationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Consultation getEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Consultation", id));
    }
}
