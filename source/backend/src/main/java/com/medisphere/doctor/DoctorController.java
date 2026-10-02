package com.medisphere.doctor;

import com.medisphere.doctor.dto.DoctorResponse;
import com.medisphere.doctor.dto.DoctorScheduleResponse;
import com.medisphere.doctor.dto.ScheduleRequest;
import com.medisphere.doctor.dto.UpdateDoctorRequest;
import com.medisphere.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/doctors")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public List<DoctorResponse> list(@RequestParam(required = false) UUID departmentId) {
        return doctorService.list(departmentId);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public DoctorResponse me() {
        return doctorService.getResponseByUserId(CurrentUser.get().getId());
    }

    @GetMapping("/{id}")
    public DoctorResponse get(@PathVariable UUID id) {
        return doctorService.get(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorResponse update(@PathVariable UUID id, @RequestBody UpdateDoctorRequest req) {
        return doctorService.update(id, req);
    }

    @GetMapping("/{id}/schedule")
    public List<DoctorScheduleResponse> schedule(@PathVariable UUID id) {
        return doctorService.getSchedule(id);
    }

    @PostMapping("/{id}/schedule")
    @PreAuthorize("hasRole('ADMIN')")
    public DoctorScheduleResponse addSchedule(@PathVariable UUID id, @Valid @RequestBody ScheduleRequest req) {
        return doctorService.addSchedule(id, req);
    }

    @DeleteMapping("/schedule/{scheduleId}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteSchedule(@PathVariable UUID scheduleId) {
        doctorService.deleteSchedule(scheduleId);
    }
}
