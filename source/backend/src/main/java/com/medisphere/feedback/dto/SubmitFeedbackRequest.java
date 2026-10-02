package com.medisphere.feedback.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record SubmitFeedbackRequest(
        @Min(1) @Max(10) int waitingRating,
        @Min(1) @Max(10) int doctorRating,
        @Min(1) @Max(10) int staffRating,
        @Min(1) @Max(10) int cleanlinessRating,
        @Min(1) @Max(10) int billingRating,
        @Min(1) @Max(10) int communicationRating,
        String comments
) {
}
