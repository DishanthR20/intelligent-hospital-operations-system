package com.medisphere.analytics;

import com.medisphere.appointment.AppointmentRepository;
import com.medisphere.bed.BedRepository;
import com.medisphere.bed.BedStatus;
import com.medisphere.bed.Ward;
import com.medisphere.bed.WardRepository;
import com.medisphere.billing.BillingService;
import com.medisphere.configuration.ConfigService;
import com.medisphere.department.Department;
import com.medisphere.department.DepartmentRepository;
import com.medisphere.doctor.DoctorRepository;
import com.medisphere.feedback.FeedbackService;
import com.medisphere.intelligence.WorkloadLevel;
import com.medisphere.queue.QueueEntry;
import com.medisphere.queue.QueueRepository;
import com.medisphere.queue.QueueStatus;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Backs the Hospital Digital Twin (section 21) and Command Center (section 22). */
@Service
@Transactional(readOnly = true)
public class AnalyticsService {

    private final DepartmentRepository departmentRepository;
    private final QueueRepository queueRepository;
    private final DoctorRepository doctorRepository;
    private final BedRepository bedRepository;
    private final WardRepository wardRepository;
    private final AppointmentRepository appointmentRepository;
    private final BillingService billingService;
    private final FeedbackService feedbackService;
    private final ConfigService configService;

    public AnalyticsService(DepartmentRepository departmentRepository, QueueRepository queueRepository,
                             DoctorRepository doctorRepository, BedRepository bedRepository, WardRepository wardRepository,
                             AppointmentRepository appointmentRepository, BillingService billingService,
                             FeedbackService feedbackService, ConfigService configService) {
        this.departmentRepository = departmentRepository;
        this.queueRepository = queueRepository;
        this.doctorRepository = doctorRepository;
        this.bedRepository = bedRepository;
        this.wardRepository = wardRepository;
        this.appointmentRepository = appointmentRepository;
        this.billingService = billingService;
        this.feedbackService = feedbackService;
        this.configService = configService;
    }

    public List<DepartmentStatus> digitalTwin() {
        return departmentRepository.findByActiveTrue().stream().map(this::statusFor).toList();
    }

    public DepartmentStatus statusFor(Department department) {
        List<QueueEntry> waiting = queueRepository.findByDepartment_IdAndStatus(department.getId(), QueueStatus.WAITING);
        long queueLength = waiting.size();
        long availableDoctors = doctorRepository.findByDepartment_IdAndActiveTrue(department.getId()).size();
        double avgWaiting = waiting.stream().mapToLong(QueueEntry::waitingMinutes).average().orElse(0);

        int moderateThreshold = configService.getInt("department.queue.threshold.moderate", 5);
        int highThreshold = configService.getInt("department.queue.threshold.high", 10);
        int criticalThreshold = configService.getInt("department.queue.threshold.critical", 15);

        WorkloadLevel level;
        if (queueLength >= criticalThreshold) level = WorkloadLevel.CRITICAL;
        else if (queueLength >= highThreshold) level = WorkloadLevel.HIGH;
        else if (queueLength >= moderateThreshold) level = WorkloadLevel.MODERATE;
        else level = WorkloadLevel.LOW;

        return new DepartmentStatus(department.getId(), department.getName(), queueLength, availableDoctors, avgWaiting, level);
    }

    public HospitalMetrics commandCenterMetrics() {
        var dayStart = LocalDate.now(ZoneOffset.UTC).atStartOfDay().toInstant(ZoneOffset.UTC);
        var now = java.time.Instant.now();

        long patientsToday = queueRepository.countByCreatedAtBetween(dayStart, now);
        long appointmentsToday = appointmentRepository.countByScheduledAtBetween(
                LocalDate.now().atStartOfDay(), LocalDate.now().plusDays(1).atStartOfDay());
        long emergencyToday = queueRepository.countByEmergencyTrueAndCreatedAtBetween(dayStart, now);

        double avgWaiting = digitalTwin().stream().mapToDouble(DepartmentStatus::avgWaitingMinutes).average().orElse(0);

        long totalBeds = bedRepository.count();
        long occupiedBeds = bedRepository.countByStatus(BedStatus.OCCUPIED);
        long bedOccupancyPercent = totalBeds == 0 ? 0 : Math.round(occupiedBeds * 100.0 / totalBeds);

        List<Ward> icuWards = wardRepository.findAll().stream().filter(Ward::isIcu).toList();
        long icuTotal = icuWards.stream().mapToLong(w -> bedRepository.countByWard_Id(w.getId())).sum();
        long icuOccupied = icuWards.stream().mapToLong(w -> bedRepository.countByWard_IdAndStatus(w.getId(), BedStatus.OCCUPIED)).sum();
        long icuOccupancyPercent = icuTotal == 0 ? 0 : Math.round(icuOccupied * 100.0 / icuTotal);

        return new HospitalMetrics(patientsToday, appointmentsToday, emergencyToday, avgWaiting,
                bedOccupancyPercent, icuOccupancyPercent, feedbackService.averageExperienceIndex(),
                billingService.revenueToday(), 0);
    }
}
