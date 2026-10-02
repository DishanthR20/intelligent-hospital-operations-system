package com.medisphere.intelligence;

import com.medisphere.common.entity.BaseEntity;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A permanent record of one explainable recommendation, so it can be replayed
 * later on the admin Decision Replay page (spec section 41).
 */
@Entity
@Table(name = "decisions")
public class Decision extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DecisionType type;

    private UUID patientId;

    @Column(nullable = false)
    private String result;

    @Column(nullable = false)
    private double score;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "decision_factors", joinColumns = @JoinColumn(name = "decision_id"))
    @OrderColumn(name = "factor_order")
    private List<DecisionFactorEmbeddable> factors = new ArrayList<>();

    protected Decision() {
    }

    public Decision(DecisionType type, UUID patientId, String result, double score, List<DecisionFactorEmbeddable> factors) {
        this.type = type;
        this.patientId = patientId;
        this.result = result;
        this.score = score;
        this.factors = factors;
    }

    public static Decision record(DecisionType type, UUID patientId, DecisionExplanation explanation) {
        List<DecisionFactorEmbeddable> factors = explanation.factors().stream()
                .map(DecisionFactorEmbeddable::from).toList();
        return new Decision(type, patientId, explanation.subject(), explanation.totalScore(), new ArrayList<>(factors));
    }

    public DecisionType getType() {
        return type;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public String getResult() {
        return result;
    }

    public double getScore() {
        return score;
    }

    public List<DecisionFactorEmbeddable> getFactors() {
        return factors;
    }
}
