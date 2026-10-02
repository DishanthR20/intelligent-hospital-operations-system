package com.medisphere.intelligence;

import com.medisphere.doctor.Doctor;
import com.medisphere.doctor.DoctorService;
import com.medisphere.patient.Patient;
import com.medisphere.patient.PatientService;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/intelligence")
public class IntelligenceController {

    private final DoctorRecommendationEngine recommendationEngine;
    private final DoctorWorkloadEngine workloadEngine;
    private final DecisionRecorder decisionRecorder;
    private final PatientService patientService;
    private final DoctorService doctorService;

    public IntelligenceController(DoctorRecommendationEngine recommendationEngine, DoctorWorkloadEngine workloadEngine,
                                   DecisionRecorder decisionRecorder, PatientService patientService,
                                   DoctorService doctorService) {
        this.recommendationEngine = recommendationEngine;
        this.workloadEngine = workloadEngine;
        this.decisionRecorder = decisionRecorder;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }

    @GetMapping("/doctor-recommendations")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST','DOCTOR','NURSE','PATIENT')")
    public List<DecisionExplanation> recommendDoctors(@RequestParam UUID departmentId,
                                                        @RequestParam(required = false) String symptomHint,
                                                        @RequestParam(required = false) UUID patientId) {
        Patient patient = patientId != null ? patientService.getEntity(patientId) : null;
        List<DecisionExplanation> results = recommendationEngine.recommend(departmentId, symptomHint, patient);
        if (!results.isEmpty() && patientId != null) {
            decisionRecorder.record(DecisionType.DOCTOR_RECOMMENDATION, patientId, results.get(0));
        }
        return results;
    }

    @GetMapping("/workload/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public DoctorWorkload workload(@PathVariable UUID doctorId) {
        Doctor doctor = doctorService.getEntity(doctorId);
        return workloadEngine.compute(doctor);
    }
}
