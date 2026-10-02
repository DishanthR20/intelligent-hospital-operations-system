package com.medisphere.doctor.dto;

import com.medisphere.doctor.DoctorSchedule;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record DoctorScheduleResponse(UUID id, DayOfWeek dayOfWeek, LocalTime startTime, LocalTime endTime) {
    public static DoctorScheduleResponse from(DoctorSchedule s) {
        return new DoctorScheduleResponse(s.getId(), s.getDayOfWeek(), s.getStartTime(), s.getEndTime());
    }
}
