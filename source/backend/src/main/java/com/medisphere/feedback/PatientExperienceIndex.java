package com.medisphere.feedback;

import java.util.Map;

/**
 * Explainable, weighted composite of one feedback survey. Not a medical metric —
 * purely an operational satisfaction score.
 */
public record PatientExperienceIndex(int overallScore, Map<String, Integer> subScores) {
}
