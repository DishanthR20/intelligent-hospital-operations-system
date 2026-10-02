package com.medisphere.feedback;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientFeedbackRepository extends JpaRepository<PatientFeedback, UUID> {
    List<PatientFeedback> findByPatient_IdOrderByCreatedAtDesc(UUID patientId);
    List<PatientFeedback> findAllByOrderByCreatedAtDesc();
}
