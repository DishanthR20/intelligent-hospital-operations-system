package com.medisphere.billing;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    List<Invoice> findByPatient_IdOrderByCreatedAtDesc(UUID patientId);
    List<Invoice> findByCreatedAtBetween(Instant start, Instant end);
}
