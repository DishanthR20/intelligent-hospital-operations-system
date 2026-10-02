package com.medisphere.analytics;

import com.medisphere.configuration.ConfigService;
import com.medisphere.department.Department;
import com.medisphere.department.DepartmentRepository;
import com.medisphere.doctor.Doctor;
import com.medisphere.doctor.DoctorRepository;
import com.medisphere.intelligence.DoctorWorkloadEngine;
import com.medisphere.intelligence.WorkloadLevel;
import com.medisphere.laboratory.LabOrderRepository;
import com.medisphere.laboratory.LabOrderStatus;
import com.medisphere.pharmacy.MedicineService;
import com.medisphere.pharmacy.ReorderStatus;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Rule-based, explainable operational alerts for the Hospital Command Center
 * (spec section 22). Every alert here is a direct read of live operational
 * state against a configurable threshold — there is no black-box model behind
 * any of these lines, which is exactly why each one can name its own cause.
 */
@Component
@Transactional(readOnly = true)
public class IntelligenceAlertEngine {

    private final AnalyticsService analyticsService;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final DoctorWorkloadEngine workloadEngine;
    private final LabOrderRepository labOrderRepository;
    private final MedicineService medicineService;
    private final ConfigService configService;

    public IntelligenceAlertEngine(AnalyticsService analyticsService, DepartmentRepository departmentRepository,
                                    DoctorRepository doctorRepository, DoctorWorkloadEngine workloadEngine,
                                    LabOrderRepository labOrderRepository, MedicineService medicineService,
                                    ConfigService configService) {
        this.analyticsService = analyticsService;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.workloadEngine = workloadEngine;
        this.labOrderRepository = labOrderRepository;
        this.medicineService = medicineService;
        this.configService = configService;
    }

    public List<String> generate() {
        List<String> alerts = new ArrayList<>();

        for (DepartmentStatus status : analyticsService.digitalTwin()) {
            if (status.loadStatus() == WorkloadLevel.HIGH || status.loadStatus() == WorkloadLevel.CRITICAL) {
                alerts.add(status.departmentName() + " waiting time is elevated (" + status.queueLength()
                        + " patient(s) waiting, load " + status.loadStatus() + ").");
            }
        }

        HospitalMetrics metrics = analyticsService.commandCenterMetrics();
        int icuThreshold = configService.getInt("alert.icu.occupancyPercent", 85);
        if (metrics.icuOccupancyPercent() >= icuThreshold) {
            alerts.add("ICU occupancy is high (" + metrics.icuOccupancyPercent() + "%).");
        }
        int bedThreshold = configService.getInt("alert.bed.occupancyPercent", 90);
        if (metrics.bedOccupancyPercent() >= bedThreshold) {
            alerts.add("Overall bed occupancy is high (" + metrics.bedOccupancyPercent() + "%).");
        }

        int labBacklogThreshold = configService.getInt("alert.lab.pendingThreshold", 10);
        long pendingLabOrders = labOrderRepository.findByStatusNotOrderByCreatedAtAsc(LabOrderStatus.VERIFIED).stream()
                .filter(o -> o.getStatus() != LabOrderStatus.CANCELLED).count();
        if (pendingLabOrders >= labBacklogThreshold) {
            alerts.add("Laboratory processing delay detected (" + pendingLabOrders + " order(s) pending).");
        }

        medicineService.demandReport().stream()
                .filter(r -> r.status() != ReorderStatus.OK)
                .forEach(r -> alerts.add("Medicine stock for \"" + r.medicineName() + "\" is projected low: " + r.explanation()));

        for (Department department : departmentRepository.findByActiveTrue()) {
            List<Doctor> doctors = doctorRepository.findByDepartment_IdAndActiveTrue(department.getId());
            boolean hasOverloaded = doctors.stream().anyMatch(d -> {
                WorkloadLevel l = workloadEngine.compute(d).level();
                return l == WorkloadLevel.HIGH || l == WorkloadLevel.CRITICAL;
            });
            boolean hasIdle = doctors.stream().anyMatch(d -> workloadEngine.compute(d).level() == WorkloadLevel.LOW);
            if (hasOverloaded && hasIdle && doctors.size() > 1) {
                alerts.add("Doctor workload imbalance detected in " + department.getName() + ".");
            }
        }

        return alerts;
    }
}
