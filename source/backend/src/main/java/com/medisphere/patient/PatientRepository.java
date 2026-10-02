package com.medisphere.patient;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByUser_Id(UUID userId);

    @Query("select p from Patient p where lower(p.user.fullName) like lower(concat('%', :q, '%')) " +
           "or lower(p.user.email) like lower(concat('%', :q, '%')) or p.user.phone like concat('%', :q, '%')")
    Page<Patient> search(String q, Pageable pageable);
}
