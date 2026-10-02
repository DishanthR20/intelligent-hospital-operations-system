package com.medisphere.queue.dto;

import com.medisphere.intelligence.DecisionExplanation;
import com.medisphere.queue.ScoredQueueEntry;
import java.util.UUID;

public record QueueEntryResponse(
        UUID id, UUID patientId, String patientName, UUID doctorId, String tokenNumber, String status,
        boolean emergency, int clinicalRisk, long waitingMinutes, int position,
        double priorityScore, int estimatedWaitMinutes, DecisionExplanation explanation
) {
    public static QueueEntryResponse from(ScoredQueueEntry scored, int position, int estimatedWaitMinutes) {
        var e = scored.entry();
        return new QueueEntryResponse(e.getId(), e.getPatient().getId(), e.getPatient().getUser().getFullName(),
                e.getDoctor() != null ? e.getDoctor().getId() : null,
                e.getTokenNumber(), e.getStatus().name(), e.isEmergency(), e.getClinicalRisk(),
                e.waitingMinutes(), position, scored.score(), estimatedWaitMinutes, scored.explanation());
    }
}
