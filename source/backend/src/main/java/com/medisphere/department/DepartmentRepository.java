package com.medisphere.department;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    List<Department> findByActiveTrue();
    Optional<Department> findByNameIgnoreCase(String name);
}
