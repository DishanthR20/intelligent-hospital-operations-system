package com.medisphere.doctor;

import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorScheduleRepository extends JpaRepository<DoctorSchedule, UUID> {
    List<DoctorSchedule> findByDoctor_IdAndDayOfWeek(UUID doctorId, DayOfWeek dayOfWeek);
    List<DoctorSchedule> findByDoctor_Id(UUID doctorId);
}
