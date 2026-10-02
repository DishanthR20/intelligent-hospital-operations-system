package com.medisphere.patient;

import com.medisphere.common.exception.ResourceNotFoundException;
import com.medisphere.patient.dto.PatientResponse;
import com.medisphere.patient.dto.UpdatePatientRequest;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PatientService {

    private final PatientRepository patientRepository;

    public PatientService(PatientRepository patientRepository) {
        this.patientRepository = patientRepository;
    }

    @Transactional(readOnly = true)
    public Patient getEntity(UUID patientId) {
        return patientRepository.findById(patientId)
                .orElseThrow(() -> ResourceNotFoundException.of("Patient", patientId));
    }

    @Transactional(readOnly = true)
    public Patient getByUserId(UUID userId) {
        return patientRepository.findByUser_Id(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Patient", userId));
    }

    /**
     * Same lookup as {@link #getByUserId}, but maps to the DTO before the transaction
     * closes — Patient.user is a lazy association, so mapping after the method returns
     * would throw LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public PatientResponse getResponseByUserId(UUID userId) {
        return PatientResponse.from(getByUserId(userId));
    }

    @Transactional(readOnly = true)
    public PatientResponse get(UUID patientId) {
        return PatientResponse.from(getEntity(patientId));
    }

    @Transactional(readOnly = true)
    public Page<PatientResponse> search(String query, Pageable pageable) {
        String q = query == null ? "" : query;
        return patientRepository.search(q, pageable).map(PatientResponse::from);
    }

    public PatientResponse update(UUID patientId, UpdatePatientRequest req) {
        Patient patient = getEntity(patientId);
        if (req.phone() != null) patient.getUser().setPhone(req.phone());
        if (req.bloodGroup() != null) patient.setBloodGroup(req.bloodGroup());
        if (req.address() != null) patient.setAddress(req.address());
        if (req.emergencyContactName() != null) patient.setEmergencyContactName(req.emergencyContactName());
        if (req.emergencyContactPhone() != null) patient.setEmergencyContactPhone(req.emergencyContactPhone());
        if (req.insuranceProvider() != null) patient.setInsuranceProvider(req.insuranceProvider());
        if (req.insurancePolicyNumber() != null) patient.setInsurancePolicyNumber(req.insurancePolicyNumber());
        if (req.allergies() != null) {
            patient.getAllergies().clear();
            patient.getAllergies().addAll(req.allergies());
        }
        if (req.chronicConditions() != null) {
            patient.getChronicConditions().clear();
            patient.getChronicConditions().addAll(req.chronicConditions());
        }
        if (req.currentMedications() != null) {
            patient.getCurrentMedications().clear();
            patient.getCurrentMedications().addAll(req.currentMedications());
        }
        return PatientResponse.from(patient);
    }
}
