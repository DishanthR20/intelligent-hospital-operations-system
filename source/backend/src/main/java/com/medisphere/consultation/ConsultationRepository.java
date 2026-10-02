package com.medisphere.consultation;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultationRepository extends JpaRepository<Consultation, UUID> {
    List<Consultation> findByPatient_IdOrderByCreatedAtDesc(UUID patientId);
    List<Consultation> findByDoctor_IdOrderByCreatedAtDesc(UUID doctorId);
}
