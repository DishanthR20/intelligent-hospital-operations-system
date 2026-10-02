package com.medisphere.intelligence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/** Persisted counterpart of {@link DecisionFactor}, stored in the decision_factors collection table. */
@Embeddable
public class DecisionFactorEmbeddable {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double points;

    @Column(length = 500)
    private String reason;

    protected DecisionFactorEmbeddable() {
    }

    public DecisionFactorEmbeddable(String name, double points, String reason) {
        this.name = name;
        this.points = points;
        this.reason = reason;
    }

    public static DecisionFactorEmbeddable from(DecisionFactor f) {
        return new DecisionFactorEmbeddable(f.name(), f.points(), f.reason());
    }

    public String getName() {
        return name;
    }

    public double getPoints() {
        return points;
    }

    public String getReason() {
        return reason;
    }
}
