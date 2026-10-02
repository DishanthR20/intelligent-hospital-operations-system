package com.medisphere.doctor.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

public record ScheduleRequest(DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
}
