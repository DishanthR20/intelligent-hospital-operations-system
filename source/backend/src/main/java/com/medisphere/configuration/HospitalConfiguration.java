package com.medisphere.configuration;

import com.medisphere.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * A single tunable hospital policy value (a queue weight, a threshold, a duration…).
 * Every intelligence engine reads its parameters from here instead of hardcoding
 * them, so an administrator can retune behaviour without a code change.
 */
@Entity
@Table(name = "hospital_configurations")
public class HospitalConfiguration extends BaseEntity {

    // Columns named "config_key"/"config_value" rather than the bare reserved
    // words KEY/VALUE, which several SQL dialects (H2 included) refuse to parse
    // unquoted in a select list.
    @Column(name = "config_key", nullable = false, unique = true)
    private String key;

    @Column(name = "config_value", nullable = false)
    private String value;

    private String description;

    protected HospitalConfiguration() {
    }

    public HospitalConfiguration(String key, String value, String description) {
        this.key = key;
        this.value = value;
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDescription() {
        return description;
    }
}
