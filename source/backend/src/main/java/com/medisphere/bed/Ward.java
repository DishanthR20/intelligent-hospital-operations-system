package com.medisphere.bed;

import com.medisphere.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "wards")
public class Ward extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private boolean icu = false;

    @Column(nullable = false)
    private boolean isolation = false;

    private String genderPolicy; // e.g. "MALE", "FEMALE", "ANY" — hospital-configured ward rule

    protected Ward() {
    }

    public Ward(String name, boolean icu, boolean isolation, String genderPolicy) {
        this.name = name;
        this.icu = icu;
        this.isolation = isolation;
        this.genderPolicy = genderPolicy;
    }

    public String getName() {
        return name;
    }

    public boolean isIcu() {
        return icu;
    }

    public boolean isIsolation() {
        return isolation;
    }

    public String getGenderPolicy() {
        return genderPolicy;
    }
}
