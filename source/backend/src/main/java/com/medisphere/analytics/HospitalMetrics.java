package com.medisphere.analytics;

import java.math.BigDecimal;

public record HospitalMetrics(
        long patientsToday, long appointmentsToday, long emergencyCasesToday, double avgWaitingMinutes,
        long bedOccupancyPercent, long icuOccupancyPercent, double patientExperienceIndex,
        BigDecimal revenueToday, long pharmacyStockAlerts
) {
}
