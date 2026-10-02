package com.medisphere.laboratory;

import com.medisphere.audit.AuditAction;
import com.medisphere.audit.AuditService;
import com.medisphere.common.exception.ResourceNotFoundException;
import com.medisphere.doctor.Doctor;
import com.medisphere.doctor.DoctorService;
import com.medisphere.event.LabResultCompletedEvent;
import com.medisphere.laboratory.dto.LabOrderResponse;
import com.medisphere.notification.Notification;
import com.medisphere.notification.NotificationService;
import com.medisphere.patient.Patient;
import com.medisphere.patient.PatientService;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LabService {

    private final LabOrderRepository orderRepository;
    private final LabTestCatalogRepository catalogRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final ApplicationEventPublisher eventPublisher;
    private final NotificationService notificationService;
    private final AuditService auditService;

    public LabService(LabOrderRepository orderRepository, LabTestCatalogRepository catalogRepository,
                       PatientService patientService, DoctorService doctorService,
                       ApplicationEventPublisher eventPublisher, NotificationService notificationService,
                       AuditService auditService) {
        this.orderRepository = orderRepository;
        this.catalogRepository = catalogRepository;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.eventPublisher = eventPublisher;
        this.notificationService = notificationService;
        this.auditService = auditService;
    }

    public LabTestCatalog addCatalogEntry(LabTestCatalog entry) {
        return catalogRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public List<LabTestCatalog> catalog() {
        return catalogRepository.findAll();
    }

    public LabOrderResponse order(UUID patientId, UUID doctorId, UUID testId) {
        Patient patient = patientService.getEntity(patientId);
        Doctor doctor = doctorService.getEntity(doctorId);
        LabTestCatalog test = catalogRepository.findById(testId)
                .orElseThrow(() -> ResourceNotFoundException.of("LabTest", testId));
        return LabOrderResponse.from(orderRepository.save(new LabOrder(patient, doctor, test)));
    }

    public LabOrderResponse collectSample(UUID orderId) {
        LabOrder order = getEntity(orderId);
        String sampleId = "SMP-" + LocalDate.now().getDayOfYear() + "-" + order.getId().toString().substring(0, 6).toUpperCase();
        order.collectSample(sampleId);
        return LabOrderResponse.from(order);
    }

    public LabOrderResponse startProcessing(UUID orderId) {
        LabOrder order = getEntity(orderId);
        order.startProcessing();
        return LabOrderResponse.from(order);
    }

    public LabOrderResponse enterResult(UUID orderId, double value, String notes) {
        LabOrder order = getEntity(orderId);
        order.enterResult(value, notes);

        eventPublisher.publishEvent(new LabResultCompletedEvent(order.getPatient().getId(), order.getTest().getName(), order.isCritical()));
        if (order.isCritical()) {
            notificationService.notify(order.getOrderingDoctor().getUser().getId(),
                    "CRITICAL RESULT ALERT: " + order.getTest().getName() + " for " + order.getPatient().getUser().getFullName()
                            + " is outside the safe range. Review required.",
                    Notification.Priority.CRITICAL);
        }
        return LabOrderResponse.from(order);
    }

    public LabOrderResponse verify(UUID orderId) {
        LabOrder order = getEntity(orderId);
        order.verify();
        auditService.record(AuditAction.VIEW_LAB_REPORT, "LabOrder", orderId.toString());
        return LabOrderResponse.from(order);
    }

    public void cancel(UUID orderId) {
        getEntity(orderId).cancel();
    }

    @Transactional(readOnly = true)
    public List<LabOrderResponse> forPatient(UUID patientId) {
        auditService.record(AuditAction.VIEW_LAB_REPORT, "Patient", patientId.toString());
        return orderRepository.findByPatient_IdOrderByCreatedAtDesc(patientId).stream().map(LabOrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<LabOrderResponse> pending() {
        return orderRepository.findByStatusNotOrderByCreatedAtAsc(LabOrderStatus.VERIFIED).stream()
                .map(LabOrderResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public LabOrder getEntity(UUID id) {
        return orderRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("LabOrder", id));
    }
}
