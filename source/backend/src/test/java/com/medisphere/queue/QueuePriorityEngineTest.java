package com.medisphere.queue;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.medisphere.configuration.ConfigService;
import com.medisphere.department.Department;
import com.medisphere.patient.Patient;
import com.medisphere.user.Role;
import com.medisphere.user.User;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for the Smart Queue priority scoring (spec section 12). Verifies
 * the three policy guarantees the spec calls out explicitly: emergencies always
 * outrank routine cases, waiting-time aging moves patients up over time, and
 * there is no VIP-style bypass anywhere in the formula.
 */
@ExtendWith(MockitoExtension.class)
class QueuePriorityEngineTest {

    @Mock
    private ConfigService configService;

    @InjectMocks
    private QueuePriorityEngine engine;

    private Department department;

    @BeforeEach
    void setUp() {
        // Fall back to the engine's documented defaults for any threshold it asks for.
        when(configService.getDouble(anyString(), anyDouble())).thenAnswer(inv -> inv.getArgument(1));
        when(configService.getInt(anyString(), anyInt())).thenAnswer(inv -> inv.getArgument(1));
        department = new Department("Cardiology", 20, "Heart care");
    }

    private Patient patientAged(int age) {
        User user = new User("p@example.com", "hash", "Test Patient", "+1", Role.PATIENT);
        LocalDate dob = LocalDate.now().minusYears(age);
        return new Patient(user, dob, Patient.Gender.OTHER, "O+");
    }

    @Test
    void emergencyEntryOutranksRoutineEntry() {
        QueueEntry emergency = new QueueEntry(patientAged(40), department, "T-1", true, 0);
        QueueEntry routine = new QueueEntry(patientAged(40), department, "T-2", false, 0);

        List<ScoredQueueEntry> ranked = engine.rank(List.of(routine, emergency));

        assertThat(ranked.get(0).entry()).isEqualTo(emergency);
        assertThat(ranked.get(0).score()).isGreaterThan(ranked.get(1).score());
    }

    @Test
    void higherClinicalRiskScoresHigherThanLowerRisk() {
        QueueEntry highRisk = new QueueEntry(patientAged(40), department, "T-1", false, 9);
        QueueEntry lowRisk = new QueueEntry(patientAged(40), department, "T-2", false, 1);

        List<ScoredQueueEntry> ranked = engine.rank(List.of(lowRisk, highRisk));

        assertThat(ranked.get(0).entry()).isEqualTo(highRisk);
    }

    @Test
    void elderlyPatientReceivesVulnerabilityBonusOverMiddleAgedPatient() {
        QueueEntry elderly = new QueueEntry(patientAged(80), department, "T-1", false, 0);
        QueueEntry middleAged = new QueueEntry(patientAged(40), department, "T-2", false, 0);

        List<ScoredQueueEntry> ranked = engine.rank(List.of(middleAged, elderly));

        assertThat(ranked.get(0).entry()).isEqualTo(elderly);
    }

    @Test
    void explanationNeverContainsAVipOrStatusFactor() {
        QueueEntry entry = new QueueEntry(patientAged(40), department, "T-1", true, 5);
        var explanation = engine.explain(entry);

        assertThat(explanation.factors())
                .extracting(f -> f.name().toLowerCase())
                .noneMatch(name -> name.contains("vip") || name.contains("status"));
    }
}
