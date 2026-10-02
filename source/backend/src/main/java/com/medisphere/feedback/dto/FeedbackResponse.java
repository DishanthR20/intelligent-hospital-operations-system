package com.medisphere.feedback.dto;

import com.medisphere.feedback.PatientExperienceIndex;
import com.medisphere.feedback.PatientFeedback;
import java.util.UUID;

public record FeedbackResponse(UUID id, UUID patientId, String comments, PatientExperienceIndex index) {
    public static FeedbackResponse from(PatientFeedback f, PatientExperienceIndex index) {
        return new FeedbackResponse(f.getId(), f.getPatient().getId(), f.getComments(), index);
    }
}
