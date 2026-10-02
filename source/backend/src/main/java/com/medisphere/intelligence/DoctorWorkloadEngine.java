package com.medisphere.intelligence;

import com.medisphere.appointment.AppointmentRepository;
import com.medisphere.configuration.ConfigService;
import com.medisphere.doctor.Doctor;
import com.medisphere.queue.QueueRepository;
import com.medisphere.queue.QueueStatus;
import java.time.LocalDate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Doctor Workload Engine (spec section 37). Feeds both the doctor recommendation
 * engine and the appointment slot optimizer, and is shown directly on the
 * doctor's own dashboard.
 */
@Component
@Transactional(readOnly = true)
public class DoctorWorkloadEngine {

    private final AppointmentRepository appointmentRepository;
    private final QueueRepository queueRepository;
    private final ConfigService configService;

    public DoctorWorkloadEngine(AppointmentRepository appointmentRepository, QueueRepository queueRepository,
                                 ConfigService configService) {
        this.appointmentRepository = appointmentRepository;
        this.queueRepository = queueRepository;
        this.configService = configService;
    }

    public DoctorWorkload compute(Doctor doctor) {
        var dayStart = LocalDate.now().atStartOfDay();
        var dayEnd = dayStart.plusDays(1);
        long activeAppointments = appointmentRepository.countActiveForDoctorOnDay(doctor.getId(), dayStart, dayEnd);
        long waitingInQueue = queueRepository.countByDoctor_IdAndStatus(doctor.getId(), QueueStatus.WAITING);

        long total = activeAppointments + waitingInQueue;
        int moderateThreshold = configService.getInt("workload.threshold.moderate", 5);
        int highThreshold = configService.getInt("workload.threshold.high", 10);
        int criticalThreshold = configService.getInt("workload.threshold.critical", 15);

        WorkloadLevel level;
        if (total >= criticalThreshold) level = WorkloadLevel.CRITICAL;
        else if (total >= highThreshold) level = WorkloadLevel.HIGH;
        else if (total >= moderateThreshold) level = WorkloadLevel.MODERATE;
        else level = WorkloadLevel.LOW;

        return new DoctorWorkload(doctor.getId(), activeAppointments, waitingInQueue, level);
    }
}
