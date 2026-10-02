package com.medisphere.analytics;

import com.medisphere.intelligence.WorkloadLevel;
import java.util.UUID;

public record DepartmentStatus(
        UUID departmentId, String departmentName, long queueLength, long availableDoctors,
        double avgWaitingMinutes, WorkloadLevel loadStatus
) {
}
