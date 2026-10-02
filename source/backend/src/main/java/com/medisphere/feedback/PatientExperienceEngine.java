package com.medisphere.feedback;

import com.medisphere.configuration.ConfigService;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Patient Happiness / Experience Engine (spec section 19). Each 1-10 sub-rating
 * is normalized to a 0-100 sub-score, then combined with admin-configurable
 * weights (spec section 48) into one explainable overall index. This is a
 * satisfaction metric only — never presented as a clinical outcome measure.
 */
@Component
public class PatientExperienceEngine {

    private final ConfigService configService;

    public PatientExperienceEngine(ConfigService configService) {
        this.configService = configService;
    }

    public PatientExperienceIndex compute(PatientFeedback feedback) {
        Map<String, Integer> subScores = new LinkedHashMap<>();
        subScores.put("Waiting", feedback.getWaitingRating() * 10);
        subScores.put("Doctor", feedback.getDoctorRating() * 10);
        subScores.put("Staff", feedback.getStaffRating() * 10);
        subScores.put("Cleanliness", feedback.getCleanlinessRating() * 10);
        subScores.put("Billing", feedback.getBillingRating() * 10);
        subScores.put("Communication", feedback.getCommunicationRating() * 10);

        double waitingW = configService.getDouble("experience.weight.waiting", 1.0);
        double doctorW = configService.getDouble("experience.weight.doctor", 1.5);
        double staffW = configService.getDouble("experience.weight.staff", 1.0);
        double cleanlinessW = configService.getDouble("experience.weight.cleanliness", 1.0);
        double billingW = configService.getDouble("experience.weight.billing", 1.0);
        double communicationW = configService.getDouble("experience.weight.communication", 1.0);

        double weightedSum = subScores.get("Waiting") * waitingW + subScores.get("Doctor") * doctorW
                + subScores.get("Staff") * staffW + subScores.get("Cleanliness") * cleanlinessW
                + subScores.get("Billing") * billingW + subScores.get("Communication") * communicationW;
        double totalWeight = waitingW + doctorW + staffW + cleanlinessW + billingW + communicationW;

        int overall = (int) Math.round(weightedSum / totalWeight);
        return new PatientExperienceIndex(overall, subScores);
    }
}
