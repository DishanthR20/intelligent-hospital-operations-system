package com.medisphere.intelligence;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DecisionRepository extends JpaRepository<Decision, UUID> {
    Page<Decision> findAllByOrderByCreatedAtDesc(Pageable pageable);
    Page<Decision> findByTypeOrderByCreatedAtDesc(DecisionType type, Pageable pageable);
    Page<Decision> findByPatientIdOrderByCreatedAtDesc(UUID patientId, Pageable pageable);
}
