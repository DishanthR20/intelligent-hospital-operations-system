package com.medisphere.pharmacy;

import com.medisphere.audit.AuditAction;
import com.medisphere.audit.AuditService;
import com.medisphere.common.exception.ConflictException;
import com.medisphere.common.exception.ResourceNotFoundException;
import com.medisphere.consultation.Consultation;
import com.medisphere.consultation.ConsultationRepository;
import com.medisphere.doctor.Doctor;
import com.medisphere.doctor.DoctorService;
import com.medisphere.event.PrescriptionCreatedEvent;
import com.medisphere.patient.Patient;
import com.medisphere.patient.PatientService;
import com.medisphere.pharmacy.dto.CreatePrescriptionRequest;
import com.medisphere.pharmacy.dto.PendingPrescriptionItemResponse;
import com.medisphere.pharmacy.dto.PrescriptionResponse;
import com.medisphere.security.CurrentUser;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PrescriptionService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionItemRepository itemRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository batchRepository;
    private final PatientService patientService;
    private final DoctorService doctorService;
    private final ConsultationRepository consultationRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final AuditService auditService;

    public PrescriptionService(PrescriptionRepository prescriptionRepository, PrescriptionItemRepository itemRepository,
                                MedicineRepository medicineRepository, MedicineBatchRepository batchRepository,
                                PatientService patientService, DoctorService doctorService,
                                ConsultationRepository consultationRepository, ApplicationEventPublisher eventPublisher,
                                AuditService auditService) {
        this.prescriptionRepository = prescriptionRepository;
        this.itemRepository = itemRepository;
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.patientService = patientService;
        this.doctorService = doctorService;
        this.consultationRepository = consultationRepository;
        this.eventPublisher = eventPublisher;
        this.auditService = auditService;
    }

    public PrescriptionResponse create(CreatePrescriptionRequest req) {
        Patient patient = patientService.getEntity(req.patientId());
        Doctor doctor = doctorService.getByUserId(CurrentUser.get().getId());
        Consultation consultation = req.consultationId() != null
                ? consultationRepository.findById(req.consultationId())
                        .orElseThrow(() -> ResourceNotFoundException.of("Consultation", req.consultationId()))
                : null;

        Prescription prescription = new Prescription(patient, doctor, consultation);
        for (var itemReq : req.items()) {
            Medicine medicine = medicineRepository.findById(itemReq.medicineId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Medicine", itemReq.medicineId()));
            prescription.addItem(new PrescriptionItem(medicine, itemReq.dosageInstructions(), itemReq.quantity()));
        }
        prescriptionRepository.save(prescription);

        auditService.record(AuditAction.CREATE_PRESCRIPTION, "Prescription", prescription.getId().toString());
        eventPublisher.publishEvent(new PrescriptionCreatedEvent(patient.getId(), doctor.getUser().getFullName(), prescription.getItems().size()));
        return PrescriptionResponse.from(prescription);
    }

    @Transactional(readOnly = true)
    public List<PrescriptionResponse> forPatient(UUID patientId) {
        return prescriptionRepository.findByPatient_IdOrderByCreatedAtDesc(patientId).stream()
                .map(PrescriptionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<PendingPrescriptionItemResponse> pendingItems() {
        return itemRepository.findByDispensedFalseOrderByCreatedAtAsc().stream()
                .map(PendingPrescriptionItemResponse::from).toList();
    }

    /** Dispenses one prescription item using first-expiry-first-out stock, then records the consumption. */
    public void dispense(UUID itemId) {
        PrescriptionItem item = itemRepository.findById(itemId)
                .orElseThrow(() -> ResourceNotFoundException.of("PrescriptionItem", itemId));
        if (item.isDispensed()) {
            throw new ConflictException("ALREADY_DISPENSED", "This item has already been dispensed");
        }

        List<MedicineBatch> batches = batchRepository.findByMedicine_IdOrderByExpiryDateAsc(item.getMedicine().getId())
                .stream().filter(b -> !b.isExpired() && b.getQuantity() > 0).toList();

        int remaining = item.getQuantity();
        int available = batches.stream().mapToInt(MedicineBatch::getQuantity).sum();
        if (available < remaining) {
            throw new ConflictException("INSUFFICIENT_STOCK",
                    "Only " + available + " unit(s) of " + item.getMedicine().getName() + " are in stock; " + remaining + " required");
        }

        for (MedicineBatch batch : batches) {
            if (remaining <= 0) break;
            int take = Math.min(remaining, batch.getQuantity());
            batch.reduce(take);
            remaining -= take;
        }

        item.getMedicine().recordDispensed(item.getQuantity());
        item.markDispensed();
    }
}
