package com.medisphere.pharmacy;

import com.medisphere.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "pharmacy_inventory")
public class Medicine extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    private String genericName;

    @Column(nullable = false)
    private String unit;

    @Column(nullable = false)
    private BigDecimal pricePerUnit;

    @Column(nullable = false)
    private int reorderThreshold;

    @Column(nullable = false)
    private long totalDispensed = 0;

    @Column(nullable = false)
    private Instant trackingStartedAt = Instant.now();

    protected Medicine() {
    }

    public Medicine(String name, String genericName, String unit, BigDecimal pricePerUnit, int reorderThreshold) {
        this.name = name;
        this.genericName = genericName;
        this.unit = unit;
        this.pricePerUnit = pricePerUnit;
        this.reorderThreshold = reorderThreshold;
    }

    public String getName() {
        return name;
    }

    public String getGenericName() {
        return genericName;
    }

    public String getUnit() {
        return unit;
    }

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public int getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(int reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public long getTotalDispensed() {
        return totalDispensed;
    }

    public void recordDispensed(long quantity) {
        this.totalDispensed += quantity;
    }

    public Instant getTrackingStartedAt() {
        return trackingStartedAt;
    }
}
