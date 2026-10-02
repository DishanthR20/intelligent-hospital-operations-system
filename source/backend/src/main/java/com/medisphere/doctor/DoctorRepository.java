package com.medisphere.doctor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, UUID> {
    Optional<Doctor> findByUser_Id(UUID userId);
    List<Doctor> findByDepartment_IdAndActiveTrue(UUID departmentId);
    List<Doctor> findByActiveTrue();
}
