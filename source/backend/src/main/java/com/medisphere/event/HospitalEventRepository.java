package com.medisphere.event;

import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HospitalEventRepository extends JpaRepository<HospitalEvent, UUID> {
    List<HospitalEvent> findByPatientIdOrderByCreatedAtAsc(UUID patientId);
    List<HospitalEvent> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
