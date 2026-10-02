package com.medisphere.appointment;

import static org.assertj.core.api.Assertions.assertThat;

import com.medisphere.department.Department;
import com.medisphere.department.DepartmentRepository;
import com.medisphere.doctor.Doctor;
import com.medisphere.doctor.DoctorRepository;
import com.medisphere.patient.Patient;
import com.medisphere.patient.PatientRepository;
import com.medisphere.user.Role;
import com.medisphere.user.User;
import com.medisphere.user.UserRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Verifies the double-booking guard (spec section 10: "Prevent conflicting
 * appointments") at the repository level, including the edge cases that are
 * easy to get wrong: a partially-overlapping slot and an appointment that is
 * cancelled (and so must NOT count as a conflict).
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(com.medisphere.common.config.JpaAuditingConfig.class)
class AppointmentConflictTest {

    @Autowired private AppointmentRepository appointmentRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PatientRepository patientRepository;
    @Autowired private DoctorRepository doctorRepository;
    @Autowired private DepartmentRepository departmentRepository;

    private Doctor doctor;
    private Patient patient;

    private void seed() {
        Department department = departmentRepository.save(new Department("Cardiology", 10, "Heart care"));
        User doctorUser = userRepository.save(new User("doc@example.com", "hash", "Dr. Test", "+1", Role.DOCTOR));
        doctor = doctorRepository.save(new Doctor(doctorUser, department, "Cardiology", "MD", 5));
        User patientUser = userRepository.save(new User("pat@example.com", "hash", "Patient Test", "+1", Role.PATIENT));
        patient = patientRepository.save(new Patient(patientUser, LocalDate.of(1990, 1, 1), Patient.Gender.OTHER, "O+"));
    }

    @Test
    void detectsAnOverlappingAppointmentForTheSameDoctor() {
        seed();
        LocalDateTime existingStart = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        appointmentRepository.save(new Appointment(patient, doctor, existingStart, 30, "Checkup"));

        // New slot starts halfway through the existing one — a genuine overlap.
        List<Appointment> overlaps = appointmentRepository.findOverlapping(
                doctor.getId(), existingStart.plusMinutes(15), existingStart.plusMinutes(45));

        assertThat(overlaps).hasSize(1);
    }

    @Test
    void aBackToBackSlotIsNotAConflict() {
        seed();
        LocalDateTime existingStart = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        appointmentRepository.save(new Appointment(patient, doctor, existingStart, 30, "Checkup"));

        // Starts exactly when the first one ends — should be free.
        List<Appointment> overlaps = appointmentRepository.findOverlapping(
                doctor.getId(), existingStart.plusMinutes(30), existingStart.plusMinutes(60));

        assertThat(overlaps).isEmpty();
    }

    @Test
    void aCancelledAppointmentIsNeverTreatedAsAConflict() {
        seed();
        LocalDateTime existingStart = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        Appointment cancelled = new Appointment(patient, doctor, existingStart, 30, "Checkup");
        cancelled.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(cancelled);

        List<Appointment> overlaps = appointmentRepository.findOverlapping(
                doctor.getId(), existingStart, existingStart.plusMinutes(30));

        assertThat(overlaps).isEmpty();
    }
}
