package com.medisphere.configuration;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConfigurationRepository extends JpaRepository<HospitalConfiguration, UUID> {
    Optional<HospitalConfiguration> findByKey(String key);
}
