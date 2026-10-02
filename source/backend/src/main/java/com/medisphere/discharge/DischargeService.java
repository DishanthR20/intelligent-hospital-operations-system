package com.medisphere.discharge;

import com.medisphere.common.exception.BadRequestException;
import com.medisphere.common.exception.ResourceNotFoundException;
import com.medisphere.event.DischargeCompletedEvent;
import com.medisphere.patient.Patient;
import com.medisphere.patient.PatientService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DischargeService {

    private final DischargeRepository repository;
    private final PatientService patientService;
    private final ApplicationEventPublisher eventPublisher;

    public DischargeService(DischargeRepository repository, PatientService patientService,
                             ApplicationEventPublisher eventPublisher) {
        this.repository = repository;
        this.patientService = patientService;
        this.eventPublisher = eventPublisher;
    }

    public DischargeRecord start(UUID patientId) {
        Patient patient = patientService.getEntity(patientId);
        return repository.save(new DischargeRecord(patient));
    }

    /** Applies a partial set of checklist updates, e.g. {"doctorApproved": true}. */
    public DischargeRecord updateChecklist(UUID recordId, Map<String, Boolean> updates, String instructions) {
        DischargeRecord record = getEntity(recordId);
        updates.forEach((key, value) -> applyChecklistItem(record, key, value));
        if (instructions != null) record.setPatientInstructions(instructions);
        return record;
    }

    private void applyChecklistItem(DischargeRecord record, String key, boolean value) {
        switch (key) {
            case "doctorApproved" -> record.setDoctorApproved(value);
            case "labReportsReviewed" -> record.setLabReportsReviewed(value);
            case "prescriptionIssued" -> record.setPrescriptionIssued(value);
            case "billingCleared" -> record.setBillingCleared(value);
            case "pharmacyCleared" -> record.setPharmacyCleared(value);
            case "followUpScheduled" -> record.setFollowUpScheduled(value);
            case "instructionsGiven" -> record.setInstructionsGiven(value);
            default -> throw new BadRequestException("UNKNOWN_CHECKLIST_ITEM", "Unknown checklist item: " + key);
        }
    }

    public DischargeRecord complete(UUID recordId) {
        DischargeRecord record = getEntity(recordId);
        if (!record.isComplete()) {
            throw new BadRequestException("DISCHARGE_INCOMPLETE",
                    "Every checklist item must be confirmed before discharge can be completed");
        }
        record.complete();
        eventPublisher.publishEvent(new DischargeCompletedEvent(record.getPatient().getId()));
        return record;
    }

    @Transactional(readOnly = true)
    public List<DischargeRecord> forPatient(UUID patientId) {
        return repository.findByPatient_IdOrderByCreatedAtDesc(patientId);
    }

    @Transactional(readOnly = true)
    public DischargeRecord getEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("DischargeRecord", id));
    }
}
