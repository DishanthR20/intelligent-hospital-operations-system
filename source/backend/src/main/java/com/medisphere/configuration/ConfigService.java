package com.medisphere.configuration;

import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Reads/writes tunable hospital policy values. Every getter takes a hardcoded
 * fallback so the platform still works sensibly the very first time it boots,
 * before an admin has visited the Configuration Center.
 */
@Service
@Transactional
public class ConfigService {

    private final ConfigurationRepository repository;

    public ConfigService(ConfigurationRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public double getDouble(String key, double fallback) {
        return repository.findByKey(key).map(c -> Double.parseDouble(c.getValue())).orElse(fallback);
    }

    @Transactional(readOnly = true)
    public int getInt(String key, int fallback) {
        return repository.findByKey(key).map(c -> Integer.parseInt(c.getValue())).orElse(fallback);
    }

    @Transactional(readOnly = true)
    public List<HospitalConfiguration> all() {
        return repository.findAll();
    }

    public void set(String key, String value, String description) {
        HospitalConfiguration config = repository.findByKey(key)
                .orElseGet(() -> new HospitalConfiguration(key, value, description));
        config.setValue(value);
        repository.save(config);
    }

    public void ensureDefault(String key, String value, String description) {
        if (repository.findByKey(key).isEmpty()) {
            repository.save(new HospitalConfiguration(key, value, description));
        }
    }

    public void setAll(Map<String, String> values) {
        values.forEach((k, v) -> set(k, v, null));
    }
}
