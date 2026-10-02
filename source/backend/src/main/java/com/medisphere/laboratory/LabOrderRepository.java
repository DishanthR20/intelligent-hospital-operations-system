package com.medisphere.laboratory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabOrderRepository extends JpaRepository<LabOrder, UUID> {
    List<LabOrder> findByPatient_IdOrderByCreatedAtDesc(UUID patientId);
    List<LabOrder> findByStatusOrderByCreatedAtAsc(LabOrderStatus status);
    List<LabOrder> findByStatusNotOrderByCreatedAtAsc(LabOrderStatus status);
}
