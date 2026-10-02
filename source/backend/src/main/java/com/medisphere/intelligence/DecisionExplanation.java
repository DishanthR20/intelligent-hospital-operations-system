package com.medisphere.intelligence;

import java.util.List;

/**
 * The full "why" behind one recommended option: its total score and the factors
 * that add up to it. {@link #totalScore()} is derived, never stored twice.
 */
public record DecisionExplanation(String subject, List<DecisionFactor> factors) {

    public double totalScore() {
        return factors.stream().mapToDouble(DecisionFactor::points).sum();
    }
}
