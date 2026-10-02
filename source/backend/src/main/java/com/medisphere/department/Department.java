package com.medisphere.department;

import com.medisphere.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "departments")
public class Department extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private boolean active = true;

    private String description;

    /** Average consultation duration used by the appointment slot optimizer, in minutes. */
    @Column(nullable = false)
    private int defaultConsultationMinutes = 15;

    protected Department() {
    }

    public Department(String name, int capacity, String description) {
        this.name = name;
        this.capacity = capacity;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getDefaultConsultationMinutes() {
        return defaultConsultationMinutes;
    }

    public void setDefaultConsultationMinutes(int defaultConsultationMinutes) {
        this.defaultConsultationMinutes = defaultConsultationMinutes;
    }
}
