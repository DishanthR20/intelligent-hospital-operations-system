package com.medisphere.feedback;

import com.medisphere.feedback.dto.FeedbackResponse;
import com.medisphere.feedback.dto.SubmitFeedbackRequest;
import com.medisphere.patient.PatientService;
import com.medisphere.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final PatientService patientService;

    public FeedbackController(FeedbackService feedbackService, PatientService patientService) {
        this.feedbackService = feedbackService;
        this.patientService = patientService;
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public FeedbackResponse submit(@Valid @RequestBody SubmitFeedbackRequest req) {
        return feedbackService.submit(patientService.getByUserId(CurrentUser.get().getId()).getId(), req);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<FeedbackResponse> mine() {
        return feedbackService.forPatient(patientService.getByUserId(CurrentUser.get().getId()).getId());
    }

    @GetMapping("/themes")
    @PreAuthorize("hasRole('ADMIN')")
    public List<FeedbackTheme> themes() {
        return feedbackService.themes();
    }
}
