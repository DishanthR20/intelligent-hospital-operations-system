package com.medisphere.discharge;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DischargeRepository extends JpaRepository<DischargeRecord, UUID> {
    List<DischargeRecord> findByPatient_IdOrderByCreatedAtDesc(UUID patientId);
    Optional<DischargeRecord> findFirstByPatient_IdAndCompletedAtIsNullOrderByCreatedAtDesc(UUID patientId);
}
