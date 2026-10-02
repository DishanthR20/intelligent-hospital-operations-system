package com.medisphere.event;

import com.medisphere.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.util.UUID;

/** A persisted row in the hospital operations timeline (spec section 43). */
@Entity
@Table(name = "hospital_events")
public class HospitalEvent extends BaseEntity {

    @Column(nullable = false)
    private String eventType;

    @Column(nullable = false, length = 500)
    private String description;

    private UUID patientId;

    protected HospitalEvent() {
    }

    public HospitalEvent(String eventType, String description, UUID patientId) {
        this.eventType = eventType;
        this.description = description;
        this.patientId = patientId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public UUID getPatientId() {
        return patientId;
    }
}
