package com.medisphere.department;

import com.medisphere.common.exception.ResourceNotFoundException;
import com.medisphere.department.dto.DepartmentRequest;
import com.medisphere.department.dto.DepartmentResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class DepartmentService {

    private final DepartmentRepository repository;

    public DepartmentService(DepartmentRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<DepartmentResponse> listActive() {
        return repository.findByActiveTrue().stream().map(DepartmentResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Department getEntity(UUID id) {
        return repository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Department", id));
    }

    public DepartmentResponse create(DepartmentRequest req) {
        Department d = new Department(req.name(), req.capacity(), req.description());
        if (req.defaultConsultationMinutes() != null) d.setDefaultConsultationMinutes(req.defaultConsultationMinutes());
        return DepartmentResponse.from(repository.save(d));
    }

    public DepartmentResponse update(UUID id, DepartmentRequest req) {
        Department d = getEntity(id);
        d.setName(req.name());
        d.setCapacity(req.capacity());
        d.setDescription(req.description());
        if (req.defaultConsultationMinutes() != null) d.setDefaultConsultationMinutes(req.defaultConsultationMinutes());
        return DepartmentResponse.from(d);
    }

    public void deactivate(UUID id) {
        Department d = getEntity(id);
        d.setActive(false);
    }
}
