package com.medisphere.queue;

import com.medisphere.common.exception.ResourceNotFoundException;
import com.medisphere.department.Department;
import com.medisphere.department.DepartmentService;
import com.medisphere.doctor.Doctor;
import com.medisphere.doctor.DoctorService;
import com.medisphere.event.EmergencyPatientArrivedEvent;
import com.medisphere.event.PatientArrivedEvent;
import com.medisphere.intelligence.DecisionRecorder;
import com.medisphere.intelligence.DecisionType;
import com.medisphere.patient.Patient;
import com.medisphere.patient.PatientService;
import com.medisphere.queue.dto.QueueEntryResponse;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class QueueService {

    private final QueueRepository queueRepository;
    private final PatientService patientService;
    private final DepartmentService departmentService;
    private final DoctorService doctorService;
    private final QueuePriorityEngine priorityEngine;
    private final DecisionRecorder decisionRecorder;
    private final ApplicationEventPublisher eventPublisher;

    public QueueService(QueueRepository queueRepository, PatientService patientService,
                         DepartmentService departmentService, DoctorService doctorService,
                         QueuePriorityEngine priorityEngine, DecisionRecorder decisionRecorder,
                         ApplicationEventPublisher eventPublisher) {
        this.queueRepository = queueRepository;
        this.patientService = patientService;
        this.departmentService = departmentService;
        this.doctorService = doctorService;
        this.priorityEngine = priorityEngine;
        this.decisionRecorder = decisionRecorder;
        this.eventPublisher = eventPublisher;
    }

    public QueueEntry addToQueue(UUID patientId, UUID departmentId, boolean emergency, int clinicalRisk) {
        Patient patient = patientService.getEntity(patientId);
        Department department = departmentService.getEntity(departmentId);
        String token = generateToken(department);

        QueueEntry entry = new QueueEntry(patient, department, token, emergency, clinicalRisk);
        queueRepository.save(entry);

        decisionRecorder.record(DecisionType.QUEUE_PRIORITY, patient.getId(), priorityEngine.explain(entry));

        if (emergency) {
            eventPublisher.publishEvent(new EmergencyPatientArrivedEvent(patient.getId(),
                    clinicalRisk >= 8 ? "CRITICAL" : clinicalRisk >= 5 ? "HIGH" : "MODERATE"));
        } else {
            eventPublisher.publishEvent(new PatientArrivedEvent(patient.getId(), department.getId(), department.getName(), token));
        }
        return entry;
    }

    @Transactional(readOnly = true)
    public List<QueueEntryResponse> getRankedQueue(UUID departmentId) {
        List<QueueEntry> waiting = queueRepository.findByDepartment_IdAndStatus(departmentId, QueueStatus.WAITING);
        List<ScoredQueueEntry> ranked = priorityEngine.rank(waiting);
        int avgConsultMinutes = departmentService.getEntity(departmentId).getDefaultConsultationMinutes();

        return java.util.stream.IntStream.range(0, ranked.size())
                .mapToObj(i -> QueueEntryResponse.from(ranked.get(i), i + 1, i * avgConsultMinutes))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<QueueEntryResponse> getEmergencyQueue() {
        List<QueueEntry> emergencyWaiting = queueRepository.findByStatus(QueueStatus.WAITING).stream()
                .filter(QueueEntry::isEmergency).toList();
        List<ScoredQueueEntry> ranked = priorityEngine.rank(emergencyWaiting);
        return java.util.stream.IntStream.range(0, ranked.size())
                .mapToObj(i -> QueueEntryResponse.from(ranked.get(i), i + 1, 0))
                .toList();
    }

    /** Nurse triage: update the clinical risk score after initial assessment. */
    public QueueEntry updateClinicalRisk(UUID queueEntryId, int clinicalRisk) {
        QueueEntry entry = getEntity(queueEntryId);
        entry.setClinicalRisk(clinicalRisk);
        return entry;
    }

    public QueueEntry assignDoctor(UUID queueEntryId, UUID doctorId) {
        QueueEntry entry = getEntity(queueEntryId);
        Doctor doctor = doctorService.getEntity(doctorId);
        entry.setDoctor(doctor);
        return entry;
    }

    public QueueEntry startConsultation(UUID queueEntryId) {
        QueueEntry entry = getEntity(queueEntryId);
        entry.startConsultation();
        return entry;
    }

    public QueueEntry complete(UUID queueEntryId) {
        QueueEntry entry = getEntity(queueEntryId);
        entry.complete();
        return entry;
    }

    @Transactional(readOnly = true)
    public QueueEntry getEntity(UUID id) {
        return queueRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("QueueEntry", id));
    }

    private String generateToken(Department department) {
        long todayCount = queueRepository.countByDepartment_IdAndStatus(department.getId(), QueueStatus.WAITING)
                + queueRepository.countByDepartment_IdAndStatus(department.getId(), QueueStatus.IN_CONSULTATION)
                + queueRepository.countByDepartment_IdAndStatus(department.getId(), QueueStatus.COMPLETED);
        String prefix = department.getName().length() >= 3
                ? department.getName().substring(0, 3).toUpperCase()
                : department.getName().toUpperCase();
        return prefix + "-" + LocalDate.now(ZoneOffset.UTC).getDayOfYear() + "-" + String.format("%03d", todayCount + 1);
    }
}
