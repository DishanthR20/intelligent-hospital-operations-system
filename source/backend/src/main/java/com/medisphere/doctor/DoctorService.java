package com.medisphere.doctor;

import com.medisphere.common.exception.ResourceNotFoundException;
import com.medisphere.department.Department;
import com.medisphere.department.DepartmentService;
import com.medisphere.doctor.dto.DoctorResponse;
import com.medisphere.doctor.dto.DoctorScheduleResponse;
import com.medisphere.doctor.dto.ScheduleRequest;
import com.medisphere.doctor.dto.UpdateDoctorRequest;
import com.medisphere.user.User;
import java.time.DayOfWeek;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final DoctorScheduleRepository scheduleRepository;
    private final DepartmentService departmentService;

    public DoctorService(DoctorRepository doctorRepository, DoctorScheduleRepository scheduleRepository,
                          DepartmentService departmentService) {
        this.doctorRepository = doctorRepository;
        this.scheduleRepository = scheduleRepository;
        this.departmentService = departmentService;
    }

    public Doctor createProfile(User user, UUID departmentId, String specialization, String qualification, int experienceYears) {
        Department department = departmentService.getEntity(departmentId);
        Doctor doctor = new Doctor(user, department, specialization, qualification, experienceYears);
        return doctorRepository.save(doctor);
    }

    @Transactional(readOnly = true)
    public Doctor getEntity(UUID id) {
        return doctorRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Doctor", id));
    }

    @Transactional(readOnly = true)
    public Doctor getByUserId(UUID userId) {
        return doctorRepository.findByUser_Id(userId).orElseThrow(() -> ResourceNotFoundException.of("Doctor", userId));
    }

    /**
     * Same lookup as {@link #getByUserId}, but maps to the DTO before the transaction
     * (and its Hibernate session) closes — the User/Department associations on Doctor
     * are lazy, so building the DTO after returning to the controller would throw
     * LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public DoctorResponse getResponseByUserId(UUID userId) {
        return DoctorResponse.from(getByUserId(userId));
    }

    @Transactional(readOnly = true)
    public List<DoctorResponse> list(UUID departmentId) {
        List<Doctor> doctors = departmentId != null
                ? doctorRepository.findByDepartment_IdAndActiveTrue(departmentId)
                : doctorRepository.findByActiveTrue();
        return doctors.stream().map(DoctorResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public DoctorResponse get(UUID id) {
        return DoctorResponse.from(getEntity(id));
    }

    public DoctorResponse update(UUID id, UpdateDoctorRequest req) {
        Doctor doctor = getEntity(id);
        if (req.departmentId() != null) doctor.setDepartment(departmentService.getEntity(req.departmentId()));
        if (req.specialization() != null) doctor.setSpecialization(req.specialization());
        if (req.qualification() != null) doctor.setQualification(req.qualification());
        if (req.experienceYears() != null) doctor.setExperienceYears(req.experienceYears());
        if (req.consultationMinutes() != null) doctor.setConsultationMinutes(req.consultationMinutes());
        if (req.active() != null) doctor.setActive(req.active());
        return DoctorResponse.from(doctor);
    }

    public DoctorScheduleResponse addSchedule(UUID doctorId, ScheduleRequest req) {
        Doctor doctor = getEntity(doctorId);
        DoctorSchedule saved = scheduleRepository.save(new DoctorSchedule(doctor, req.dayOfWeek(), req.startTime(), req.endTime()));
        return DoctorScheduleResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<DoctorScheduleResponse> getSchedule(UUID doctorId) {
        return scheduleRepository.findByDoctor_Id(doctorId).stream().map(DoctorScheduleResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<DoctorSchedule> getScheduleForDay(UUID doctorId, DayOfWeek day) {
        return scheduleRepository.findByDoctor_IdAndDayOfWeek(doctorId, day);
    }

    public void deleteSchedule(UUID scheduleId) {
        scheduleRepository.deleteById(scheduleId);
    }
}
