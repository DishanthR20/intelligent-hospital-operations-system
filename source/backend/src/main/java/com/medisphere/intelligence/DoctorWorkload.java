package com.medisphere.intelligence;

import java.util.UUID;

public record DoctorWorkload(UUID doctorId, long activePatients, long waitingInQueue, WorkloadLevel level) {
}
