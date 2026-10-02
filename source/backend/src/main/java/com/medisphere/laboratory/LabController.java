package com.medisphere.laboratory;

import com.medisphere.doctor.DoctorService;
import com.medisphere.laboratory.dto.CatalogEntryRequest;
import com.medisphere.laboratory.dto.LabOrderResponse;
import com.medisphere.laboratory.dto.EnterResultRequest;
import com.medisphere.laboratory.dto.OrderLabTestRequest;
import com.medisphere.patient.PatientService;
import com.medisphere.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/laboratory")
public class LabController {

    private final LabService labService;
    private final PatientService patientService;
    private final DoctorService doctorService;

    public LabController(LabService labService, PatientService patientService, DoctorService doctorService) {
        this.labService = labService;
        this.patientService = patientService;
        this.doctorService = doctorService;
    }

    @GetMapping("/catalog")
    public List<LabTestCatalog> catalog() {
        return labService.catalog();
    }

    @PostMapping("/catalog")
    @PreAuthorize("hasRole('ADMIN')")
    public LabTestCatalog addCatalogEntry(@Valid @RequestBody CatalogEntryRequest req) {
        return labService.addCatalogEntry(new LabTestCatalog(req.name(), req.price(), req.unit(),
                req.normalRangeLow(), req.normalRangeHigh(), req.criticalLow(), req.criticalHigh()));
    }

    @PostMapping("/orders")
    @PreAuthorize("hasRole('DOCTOR')")
    public LabOrderResponse order(@Valid @RequestBody OrderLabTestRequest req) {
        UUID orderingDoctorId = doctorService.getByUserId(CurrentUser.get().getId()).getId();
        return labService.order(req.patientId(), orderingDoctorId, req.testId());
    }

    @PostMapping("/orders/{id}/collect-sample")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','ADMIN')")
    public LabOrderResponse collectSample(@PathVariable UUID id) {
        return labService.collectSample(id);
    }

    @PostMapping("/orders/{id}/start-processing")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','ADMIN')")
    public LabOrderResponse startProcessing(@PathVariable UUID id) {
        return labService.startProcessing(id);
    }

    @PostMapping("/orders/{id}/result")
    @PreAuthorize("hasAnyRole('LAB_TECHNICIAN','ADMIN')")
    public LabOrderResponse enterResult(@PathVariable UUID id, @Valid @RequestBody EnterResultRequest req) {
        return labService.enterResult(id, req.value(), req.notes());
    }

    @PostMapping("/orders/{id}/verify")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public LabOrderResponse verify(@PathVariable UUID id) {
        return labService.verify(id);
    }

    @PostMapping("/orders/{id}/cancel")
    @PreAuthorize("hasAnyRole('DOCTOR','ADMIN')")
    public void cancel(@PathVariable UUID id) {
        labService.cancel(id);
    }

    @GetMapping("/orders/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','LAB_TECHNICIAN','PATIENT')")
    public List<LabOrderResponse> forPatient(@PathVariable UUID patientId) {
        return labService.forPatient(patientId);
    }

    @GetMapping("/orders/me")
    @PreAuthorize("hasRole('PATIENT')")
    public List<LabOrderResponse> mine() {
        return labService.forPatient(patientService.getByUserId(CurrentUser.get().getId()).getId());
    }

    @GetMapping("/orders/pending")
    @PreAuthorize("hasAnyRole('ADMIN','LAB_TECHNICIAN','DOCTOR')")
    public List<LabOrderResponse> pending() {
        return labService.pending();
    }
}
