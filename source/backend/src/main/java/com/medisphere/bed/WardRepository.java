package com.medisphere.bed;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WardRepository extends JpaRepository<Ward, UUID> {
    List<Ward> findAll();
}
