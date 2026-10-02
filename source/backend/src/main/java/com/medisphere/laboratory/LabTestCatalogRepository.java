package com.medisphere.laboratory;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LabTestCatalogRepository extends JpaRepository<LabTestCatalog, UUID> {
    List<LabTestCatalog> findAll();
}
