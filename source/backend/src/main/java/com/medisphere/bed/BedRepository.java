package com.medisphere.bed;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BedRepository extends JpaRepository<Bed, UUID> {
    List<Bed> findByWard_Id(UUID wardId);
    List<Bed> findByWard_IdAndStatus(UUID wardId, BedStatus status);
    List<Bed> findByStatus(BedStatus status);
    long countByWard_Id(UUID wardId);
    long countByWard_IdAndStatus(UUID wardId, BedStatus status);
    long countByStatus(BedStatus status);
}
