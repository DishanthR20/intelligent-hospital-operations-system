package com.medisphere.config;

import com.medisphere.bed.Bed;
import com.medisphere.bed.BedRepository;
import com.medisphere.bed.Ward;
import com.medisphere.bed.WardRepository;
import com.medisphere.configuration.ConfigService;
import com.medisphere.department.Department;
import com.medisphere.department.DepartmentRepository;
import com.medisphere.doctor.Doctor;
import com.medisphere.doctor.DoctorRepository;
import com.medisphere.doctor.DoctorSchedule;
import com.medisphere.doctor.DoctorScheduleRepository;
import com.medisphere.laboratory.LabTestCatalog;
import com.medisphere.laboratory.LabTestCatalogRepository;
import com.medisphere.patient.Patient;
import com.medisphere.patient.PatientRepository;
import com.medisphere.pharmacy.Medicine;
import com.medisphere.pharmacy.MedicineBatch;
import com.medisphere.pharmacy.MedicineBatchRepository;
import com.medisphere.pharmacy.MedicineRepository;
import com.medisphere.queue.QueueEntry;
import com.medisphere.queue.QueueRepository;
import com.medisphere.user.Role;
import com.medisphere.user.User;
import com.medisphere.user.UserRepository;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Seeds realistic demo data on first boot, including the scenario described in
 * spec section 61: Cardiology queue high, ICU occupancy high, doctor workload
 * imbalanced, laboratory queue moderate — so the Intelligence Alert Engine has
 * something meaningful to say the very first time an admin opens the dashboard.
 * Idempotent: skips entirely if any user already exists.
 */
@Component
@Transactional
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final WardRepository wardRepository;
    private final BedRepository bedRepository;
    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository batchRepository;
    private final LabTestCatalogRepository labTestRepository;
    private final QueueRepository queueRepository;
    private final ConfigService configService;
    private final PasswordEncoder passwordEncoder;

    @Value("${medisphere.seed.enabled}")
    private boolean seedEnabled;

    public DataSeeder(UserRepository userRepository, PatientRepository patientRepository,
                       DepartmentRepository departmentRepository, DoctorRepository doctorRepository,
                       DoctorScheduleRepository scheduleRepository, WardRepository wardRepository,
                       BedRepository bedRepository, MedicineRepository medicineRepository,
                       MedicineBatchRepository batchRepository, LabTestCatalogRepository labTestRepository,
                       QueueRepository queueRepository, ConfigService configService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
        this.wardRepository = wardRepository;
        this.bedRepository = bedRepository;
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.labTestRepository = labTestRepository;
        this.queueRepository = queueRepository;
        this.configService = configService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedDefaultConfiguration();
        if (!seedEnabled || userRepository.count() > 0) {
            return;
        }

        // --- Admin ---
        createUser("admin@medisphere.ai", "Admin123!", "Ava Administrator", "+1-555-0100", Role.ADMIN);

        // --- Departments ---
        Department cardiology = departmentRepository.save(new Department("Cardiology", 40, "Heart and cardiovascular care"));
        Department neurology = departmentRepository.save(new Department("Neurology", 30, "Brain and nervous system care"));
        Department generalMedicine = departmentRepository.save(new Department("General Medicine", 50, "Primary care and general consultations"));
        Department pediatrics = departmentRepository.save(new Department("Pediatrics", 25, "Child healthcare"));
        Department emergency = departmentRepository.save(new Department("Emergency", 20, "Emergency and trauma care"));
        Department orthopedics = departmentRepository.save(new Department("Orthopedics", 20, "Bone and joint care"));
        Department dermatology = departmentRepository.save(new Department("Dermatology", 15, "Skin care"));

        // --- Doctors (with schedules Mon-Fri 09:00-17:00) ---
        Doctor cardio1 = createDoctor("dr.arun@medisphere.ai", "Arun Kumar", "+1-555-0201", cardiology,
                "Cardiology", "MD Cardiology", 12);
        Doctor cardio2 = createDoctor("dr.mehta@medisphere.ai", "Sanjay Mehta", "+1-555-0202", cardiology,
                "Interventional Cardiology", "MD, DM Cardiology", 18);
        Doctor neuro1 = createDoctor("dr.chen@medisphere.ai", "Lin Chen", "+1-555-0203", neurology,
                "Neurology", "MD Neurology", 9);
        Doctor gen1 = createDoctor("dr.patel@medisphere.ai", "Nisha Patel", "+1-555-0204", generalMedicine,
                "General Medicine", "MBBS, MD", 6);
        Doctor gen2 = createDoctor("dr.smith@medisphere.ai", "John Smith", "+1-555-0205", generalMedicine,
                "Family Medicine", "MBBS, MD", 4);
        Doctor peds1 = createDoctor("dr.garcia@medisphere.ai", "Maria Garcia", "+1-555-0206", pediatrics,
                "Pediatrics", "MD Pediatrics", 10);
        Doctor emerg1 = createDoctor("dr.khan@medisphere.ai", "Imran Khan", "+1-555-0207", emergency,
                "Emergency Medicine", "MD Emergency Medicine", 8);
        createDoctor("dr.lopez@medisphere.ai", "Carlos Lopez", "+1-555-0208", orthopedics,
                "Orthopedic Surgery", "MS Orthopedics", 14);
        createDoctor("dr.wong@medisphere.ai", "Amy Wong", "+1-555-0209", dermatology,
                "Dermatology", "MD Dermatology", 7);

        // --- Other staff ---
        createUser("nurse.taylor@medisphere.ai", "Nurse123!", "Taylor Reed", "+1-555-0301", Role.NURSE);
        createUser("reception.jones@medisphere.ai", "Reception123!", "Jamie Jones", "+1-555-0302", Role.RECEPTIONIST);
        createUser("pharmacist.lee@medisphere.ai", "Pharmacy123!", "Grace Lee", "+1-555-0303", Role.PHARMACIST);
        createUser("labtech.brown@medisphere.ai", "LabTech123!", "Casey Brown", "+1-555-0304", Role.LAB_TECHNICIAN);

        // --- Patients ---
        Patient patient1 = createPatient("john.doe@example.com", "Patient123!", "John Doe", "+1-555-0401",
                LocalDate.of(1985, 4, 12), Patient.Gender.MALE, "O+");
        Patient patient2 = createPatient("mary.jane@example.com", "Patient123!", "Mary Jane", "+1-555-0402",
                LocalDate.of(1958, 11, 2), Patient.Gender.FEMALE, "A-");
        Patient patient3 = createPatient("sam.wilson@example.com", "Patient123!", "Sam Wilson", "+1-555-0403",
                LocalDate.of(2019, 6, 30), Patient.Gender.MALE, "B+");
        Patient patient4 = createPatient("priya.rao@example.com", "Patient123!", "Priya Rao", "+1-555-0404",
                LocalDate.of(1990, 1, 20), Patient.Gender.FEMALE, "AB+");
        Patient patient5 = createPatient("tom.baker@example.com", "Patient123!", "Tom Baker", "+1-555-0405",
                LocalDate.of(1975, 8, 15), Patient.Gender.MALE, "O-");

        patient1.getAllergies().add("Penicillin");
        patient2.getChronicConditions().add("Hypertension");
        patient2.getCurrentMedications().add("Amlodipine 5mg");

        // --- Wards & Beds ---
        Ward icuWard = wardRepository.save(new Ward("ICU", true, false, "ANY"));
        Ward generalWardA = wardRepository.save(new Ward("General Ward A", false, false, "MALE"));
        Ward generalWardB = wardRepository.save(new Ward("General Ward B", false, false, "FEMALE"));
        Ward isolationWard = wardRepository.save(new Ward("Isolation Ward", false, true, "ANY"));

        for (int i = 1; i <= 5; i++) bedRepository.save(new Bed(icuWard, "ICU-" + i));
        for (int i = 1; i <= 10; i++) bedRepository.save(new Bed(generalWardA, "GWA-" + i));
        for (int i = 1; i <= 10; i++) bedRepository.save(new Bed(generalWardB, "GWB-" + i));
        for (int i = 1; i <= 4; i++) bedRepository.save(new Bed(isolationWard, "ISO-" + i));

        // Occupy most ICU beds to demonstrate the "ICU occupancy is HIGH" alert.
        java.util.List<Bed> icuBeds = bedRepository.findByWard_IdAndStatus(icuWard.getId(), com.medisphere.bed.BedStatus.AVAILABLE);
        for (int i = 0; i < 4 && i < icuBeds.size(); i++) {
            icuBeds.get(i).occupy(java.util.UUID.randomUUID());
        }

        // --- Pharmacy ---
        Medicine paracetamol = medicineRepository.save(new Medicine("Paracetamol 500mg", "Paracetamol", "tablet",
                new BigDecimal("0.20"), 200));
        batchRepository.save(new MedicineBatch(paracetamol, "PC-2026-01", LocalDate.now().plusYears(1), "PharmaCorp", 1000));

        Medicine amoxicillin = medicineRepository.save(new Medicine("Amoxicillin 250mg", "Amoxicillin", "capsule",
                new BigDecimal("0.45"), 100));
        batchRepository.save(new MedicineBatch(amoxicillin, "AX-2026-02", LocalDate.now().plusMonths(8), "MediSupply", 40));
        amoxicillin.recordDispensed(90); // pushes it toward REORDER_RECOMMENDED against a low starting stock

        Medicine insulin = medicineRepository.save(new Medicine("Insulin Glargine", "Insulin Glargine", "vial",
                new BigDecimal("18.00"), 20));
        batchRepository.save(new MedicineBatch(insulin, "IN-2026-01", LocalDate.now().plusMonths(6), "BioPharma", 15));

        // --- Lab catalog ---
        labTestRepository.save(new LabTestCatalog("Complete Blood Count", new BigDecimal("15.00"), "cells/mcL",
                4500.0, 11000.0, 2000.0, 20000.0));
        labTestRepository.save(new LabTestCatalog("Blood Glucose (Fasting)", new BigDecimal("8.00"), "mg/dL",
                70.0, 100.0, 40.0, 400.0));
        labTestRepository.save(new LabTestCatalog("Lipid Profile", new BigDecimal("20.00"), "mg/dL", 0.0, 200.0, null, 300.0));
        labTestRepository.save(new LabTestCatalog("Troponin I", new BigDecimal("35.00"), "ng/mL", 0.0, 0.04, null, 0.4));

        // --- Cardiology queue: several waiting patients to trigger the HIGH load alert ---
        seedQueueEntry(patient1, cardiology, false, 2);
        seedQueueEntry(patient2, cardiology, false, 3);
        seedQueueEntry(patient4, cardiology, true, 8);
        seedQueueEntry(patient5, cardiology, false, 1);
        seedQueueEntry(patient3, cardiology, false, 0);
        seedQueueEntry(patient1, cardiology, false, 0);
        seedQueueEntry(patient2, cardiology, false, 1);
        seedQueueEntry(patient5, cardiology, false, 0);
        seedQueueEntry(patient3, cardiology, false, 2);
        seedQueueEntry(patient4, cardiology, false, 0);

        // Assign all cardiology load to dr. Arun so dr. Mehta stays idle => workload imbalance alert.
        java.util.List<QueueEntry> cardioQueue = queueRepository.findByDepartment_IdAndStatus(cardiology.getId(), com.medisphere.queue.QueueStatus.WAITING);
        cardioQueue.forEach(q -> q.setDoctor(cardio1));
    }

    private void seedDefaultConfiguration() {
        configService.ensureDefault("queue.weight.emergency", "60", "Points added when a queue entry is marked emergency");
        configService.ensureDefault("queue.weight.clinicalRiskPerPoint", "4", "Points per clinical-risk point (0-10)");
        configService.ensureDefault("queue.weight.waitingTimePerMinute", "0.5", "Points added per minute waited (aging)");
        configService.ensureDefault("queue.weight.vulnerability.elderly", "10", "Bonus points for elderly patients");
        configService.ensureDefault("queue.weight.vulnerability.child", "8", "Bonus points for young children");
        configService.ensureDefault("queue.vulnerability.elderlyAge", "65", "Age threshold for the elderly bonus");
        configService.ensureDefault("queue.vulnerability.childAge", "5", "Age threshold for the child bonus");
        configService.ensureDefault("workload.threshold.moderate", "5", "Active-patient count where doctor workload becomes MODERATE");
        configService.ensureDefault("workload.threshold.high", "10", "Active-patient count where doctor workload becomes HIGH");
        configService.ensureDefault("workload.threshold.critical", "15", "Active-patient count where doctor workload becomes CRITICAL");
        configService.ensureDefault("department.queue.threshold.moderate", "3", "Queue length where a department becomes MODERATE load");
        configService.ensureDefault("department.queue.threshold.high", "5", "Queue length where a department becomes HIGH load");
        configService.ensureDefault("department.queue.threshold.critical", "8", "Queue length where a department becomes CRITICAL load");
        configService.ensureDefault("pharmacy.reorderDaysThreshold", "7", "Days of remaining supply that triggers a reorder recommendation");
        configService.ensureDefault("pharmacy.urgentReorderDaysThreshold", "3", "Days of remaining supply that triggers an urgent reorder");
        configService.ensureDefault("alert.icu.occupancyPercent", "80", "ICU occupancy percent that triggers a HIGH occupancy alert");
        configService.ensureDefault("alert.bed.occupancyPercent", "90", "Overall bed occupancy percent that triggers an alert");
        configService.ensureDefault("alert.lab.pendingThreshold", "3", "Pending lab order count that triggers a processing-delay alert");
        configService.ensureDefault("experience.weight.waiting", "1.0", "Weight of the waiting-time rating in the Patient Experience Index");
        configService.ensureDefault("experience.weight.doctor", "1.5", "Weight of the doctor rating in the Patient Experience Index");
        configService.ensureDefault("experience.weight.staff", "1.0", "Weight of the staff rating in the Patient Experience Index");
        configService.ensureDefault("experience.weight.cleanliness", "1.0", "Weight of the cleanliness rating in the Patient Experience Index");
        configService.ensureDefault("experience.weight.billing", "1.0", "Weight of the billing rating in the Patient Experience Index");
        configService.ensureDefault("experience.weight.communication", "1.0", "Weight of the communication rating in the Patient Experience Index");
    }

    private User createUser(String email, String password, String fullName, String phone, Role role) {
        return userRepository.save(new User(email, passwordEncoder.encode(password), fullName, phone, role));
    }

    private Doctor createDoctor(String email, String fullName, String phone, Department department,
                                 String specialization, String qualification, int experienceYears) {
        User user = createUser(email, "Doctor123!", fullName, phone, Role.DOCTOR);
        Doctor doctor = doctorRepository.save(new Doctor(user, department, specialization, qualification, experienceYears));
        for (DayOfWeek day : new DayOfWeek[]{DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            scheduleRepository.save(new DoctorSchedule(doctor, day, LocalTime.of(9, 0), LocalTime.of(17, 0)));
        }
        return doctor;
    }

    private Patient createPatient(String email, String password, String fullName, String phone,
                                   LocalDate dob, Patient.Gender gender, String bloodGroup) {
        User user = createUser(email, password, fullName, phone, Role.PATIENT);
        return patientRepository.save(new Patient(user, dob, gender, bloodGroup));
    }

    private void seedQueueEntry(Patient patient, Department department, boolean emergency, int clinicalRisk) {
        String token = department.getName().substring(0, 3).toUpperCase() + "-SEED-" + System.nanoTime() % 10000;
        queueRepository.save(new QueueEntry(patient, department, token, emergency, clinicalRisk));
    }
}
