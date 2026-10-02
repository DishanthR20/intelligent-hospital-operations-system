package com.medisphere.pharmacy;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineBatchRepository extends JpaRepository<MedicineBatch, UUID> {
    List<MedicineBatch> findByMedicine_IdOrderByExpiryDateAsc(UUID medicineId);
    List<MedicineBatch> findByExpiryDateBefore(java.time.LocalDate date);
}
