package com.medisphere.laboratory;

import com.medisphere.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;

/**
 * A billable lab test type with admin-configurable normal and critical reference
 * ranges (spec section 28: "Configurable Critical Result Alerts"). A result
 * outside the critical range triggers a notification workflow — it never drives
 * an autonomous diagnosis.
 */
@Entity
@Table(name = "lab_tests")
public class LabTestCatalog extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private BigDecimal price;

    private String unit;
    private Double normalRangeLow;
    private Double normalRangeHigh;
    private Double criticalLow;
    private Double criticalHigh;

    protected LabTestCatalog() {
    }

    public LabTestCatalog(String name, BigDecimal price, String unit, Double normalRangeLow, Double normalRangeHigh,
                           Double criticalLow, Double criticalHigh) {
        this.name = name;
        this.price = price;
        this.unit = unit;
        this.normalRangeLow = normalRangeLow;
        this.normalRangeHigh = normalRangeHigh;
        this.criticalLow = criticalLow;
        this.criticalHigh = criticalHigh;
    }

    public boolean isCritical(double value) {
        return (criticalLow != null && value < criticalLow) || (criticalHigh != null && value > criticalHigh);
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getUnit() {
        return unit;
    }

    public Double getNormalRangeLow() {
        return normalRangeLow;
    }

    public Double getNormalRangeHigh() {
        return normalRangeHigh;
    }

    public Double getCriticalLow() {
        return criticalLow;
    }

    public Double getCriticalHigh() {
        return criticalHigh;
    }
}
