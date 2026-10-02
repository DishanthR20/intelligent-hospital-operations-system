package com.medisphere.feedback;

import com.medisphere.feedback.dto.FeedbackResponse;
import com.medisphere.feedback.dto.SubmitFeedbackRequest;
import com.medisphere.patient.Patient;
import com.medisphere.patient.PatientService;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FeedbackService {

    private final PatientFeedbackRepository repository;
    private final PatientService patientService;
    private final PatientExperienceEngine experienceEngine;
    private final FeedbackInsightEngine insightEngine;

    public FeedbackService(PatientFeedbackRepository repository, PatientService patientService,
                            PatientExperienceEngine experienceEngine, FeedbackInsightEngine insightEngine) {
        this.repository = repository;
        this.patientService = patientService;
        this.experienceEngine = experienceEngine;
        this.insightEngine = insightEngine;
    }

    public FeedbackResponse submit(UUID patientId, SubmitFeedbackRequest req) {
        Patient patient = patientService.getEntity(patientId);
        PatientFeedback feedback = new PatientFeedback(patient, req.waitingRating(), req.doctorRating(),
                req.staffRating(), req.cleanlinessRating(), req.billingRating(), req.communicationRating(), req.comments());
        repository.save(feedback);
        return FeedbackResponse.from(feedback, experienceEngine.compute(feedback));
    }

    @Transactional(readOnly = true)
    public List<FeedbackResponse> forPatient(UUID patientId) {
        return repository.findByPatient_IdOrderByCreatedAtDesc(patientId).stream()
                .map(f -> FeedbackResponse.from(f, experienceEngine.compute(f))).toList();
    }

    @Transactional(readOnly = true)
    public List<FeedbackTheme> themes() {
        return insightEngine.analyze(repository.findAllByOrderByCreatedAtDesc());
    }

    @Transactional(readOnly = true)
    public double averageExperienceIndex() {
        List<PatientFeedback> all = repository.findAllByOrderByCreatedAtDesc();
        if (all.isEmpty()) return 0;
        return all.stream().mapToInt(f -> experienceEngine.compute(f).overallScore()).average().orElse(0);
    }
}
