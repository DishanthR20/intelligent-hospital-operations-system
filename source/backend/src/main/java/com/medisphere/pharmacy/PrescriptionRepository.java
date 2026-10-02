package com.medisphere.pharmacy;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, UUID> {
    List<Prescription> findByPatient_IdOrderByCreatedAtDesc(UUID patientId);
}
