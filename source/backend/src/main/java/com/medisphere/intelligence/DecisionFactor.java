package com.medisphere.intelligence;

/**
 * One weighted contributor to an explainable score, e.g. "Specialization Match: 40".
 * Every intelligence engine in MediSphere builds its recommendations out of these
 * so the reasoning can always be shown to the user, never just the final number.
 */
public record DecisionFactor(String name, double points, String reason) {
}
