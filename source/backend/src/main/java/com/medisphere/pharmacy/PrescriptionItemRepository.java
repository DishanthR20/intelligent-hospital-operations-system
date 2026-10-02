package com.medisphere.pharmacy;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, UUID> {
    List<PrescriptionItem> findByDispensedFalseOrderByCreatedAtAsc();
}
