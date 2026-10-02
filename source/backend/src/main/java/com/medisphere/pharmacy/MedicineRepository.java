package com.medisphere.pharmacy;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicineRepository extends JpaRepository<Medicine, UUID> {
    Page<Medicine> findByNameContainingIgnoreCase(String name, Pageable pageable);
    Optional<Medicine> findByNameIgnoreCase(String name);
    List<Medicine> findAll();
}
