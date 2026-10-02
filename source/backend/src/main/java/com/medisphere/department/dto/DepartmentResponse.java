package com.medisphere.department.dto;

import com.medisphere.department.Department;
import java.util.UUID;

public record DepartmentResponse(
        UUID id, String name, int capacity, boolean active, String description, int defaultConsultationMinutes
) {
    public static DepartmentResponse from(Department d) {
        return new DepartmentResponse(d.getId(), d.getName(), d.getCapacity(), d.isActive(),
                d.getDescription(), d.getDefaultConsultationMinutes());
    }
}
