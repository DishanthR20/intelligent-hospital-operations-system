package com.medisphere.intelligence;

import com.medisphere.appointment.AppointmentRepository;
import com.medisphere.doctor.Doctor;
import com.medisphere.doctor.DoctorRepository;
import com.medisphere.doctor.DoctorScheduleRepository;
import com.medisphere.patient.Patient;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Doctor Recommendation Engine (spec section 14). This is a routing/triage aid,
 * not a diagnostic tool: it never interprets symptoms medically, it only matches
 * the free-text symptom/specialty hint against each doctor's specialization
 * string and combines that with availability, current workload, experience and
 * whether this patient has seen the doctor before.
 */
@Component
@Transactional(readOnly = true)
public class DoctorRecommendationEngine {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorWorkloadEngine workloadEngine;

    public DoctorRecommendationEngine(DoctorRepository doctorRepository, DoctorScheduleRepository scheduleRepository,
                                       AppointmentRepository appointmentRepository, DoctorWorkloadEngine workloadEngine) {
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
        this.appointmentRepository = appointmentRepository;
        this.workloadEngine = workloadEngine;
    }

    public List<DecisionExplanation> recommend(UUID departmentId, String symptomHint, Patient patient) {
        List<Doctor> candidates = doctorRepository.findByDepartment_IdAndActiveTrue(departmentId);
        List<DecisionExplanation> results = new ArrayList<>();

        for (Doctor doctor : candidates) {
            List<DecisionFactor> factors = new ArrayList<>();

            factors.add(specializationMatch(doctor, symptomHint));
            factors.add(availability(doctor));
            factors.add(workload(doctor));
            factors.add(experience(doctor));
            if (patient != null) {
                factors.add(historyRelevance(doctor, patient));
            }

            results.add(new DecisionExplanation(doctor.getUser().getFullName() + " (" + doctor.getId() + ")", factors));
        }

        return results.stream().sorted(Comparator.comparingDouble(DecisionExplanation::totalScore).reversed()).toList();
    }

    private DecisionFactor specializationMatch(Doctor doctor, String hint) {
        if (hint == null || hint.isBlank()) {
            return new DecisionFactor("Specialization Match", 20, "No symptom hint given; department match only");
        }
        boolean matches = doctor.getSpecialization().toLowerCase().contains(hint.toLowerCase())
                || hint.toLowerCase().contains(doctor.getSpecialization().toLowerCase());
        double points = matches ? 40 : 15;
        return new DecisionFactor("Specialization Match", points,
                matches ? "\"" + hint + "\" matches specialization " + doctor.getSpecialization()
                        : "No direct keyword match with specialization " + doctor.getSpecialization());
    }

    private DecisionFactor availability(Doctor doctor) {
        boolean availableToday = !scheduleRepository
                .findByDoctor_IdAndDayOfWeek(doctor.getId(), DayOfWeek.from(java.time.LocalDate.now()))
                .isEmpty();
        return new DecisionFactor("Availability", availableToday ? 20 : 5,
                availableToday ? "Has a configured schedule today" : "No configured availability today");
    }

    private DecisionFactor workload(Doctor doctor) {
        WorkloadLevel level = workloadEngine.compute(doctor).level();
        double points = switch (level) {
            case LOW -> 15;
            case MODERATE -> 10;
            case HIGH -> 5;
            case CRITICAL -> 0;
        };
        return new DecisionFactor("Workload", points, "Current workload is " + level);
    }

    private DecisionFactor experience(Doctor doctor) {
        double points = Math.min(doctor.getExperienceYears(), 10);
        return new DecisionFactor("Experience", points, doctor.getExperienceYears() + " year(s) of experience");
    }

    private DecisionFactor historyRelevance(Doctor doctor, Patient patient) {
        boolean seenBefore = appointmentRepository.findByPatient_IdOrderByScheduledAtDesc(patient.getId()).stream()
                .anyMatch(a -> a.getDoctor().getId().equals(doctor.getId()));
        return new DecisionFactor("Patient History Relevance", seenBefore ? 6 : 0,
                seenBefore ? "Patient has consulted this doctor before" : "No prior consultation with this doctor");
    }
}
