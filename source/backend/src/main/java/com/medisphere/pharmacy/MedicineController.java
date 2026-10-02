package com.medisphere.pharmacy;

import com.medisphere.pharmacy.dto.AddBatchRequest;
import com.medisphere.pharmacy.dto.MedicineRequest;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/pharmacy/medicines")
@PreAuthorize("hasAnyRole('ADMIN','PHARMACIST','DOCTOR')")
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    public Page<Medicine> search(@RequestParam(required = false) String q, Pageable pageable) {
        return medicineService.search(q, pageable);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public Medicine create(@Valid @RequestBody MedicineRequest req) {
        return medicineService.create(req);
    }

    @PostMapping("/{id}/batches")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public MedicineBatch addBatch(@PathVariable UUID id, @Valid @RequestBody AddBatchRequest req) {
        return medicineService.addBatch(id, req);
    }

    @GetMapping("/{id}/batches")
    public List<MedicineBatch> batches(@PathVariable UUID id) {
        return medicineService.batchesFor(id);
    }

    @GetMapping("/demand-report")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public List<MedicineDemandReport> demandReport() {
        return medicineService.demandReport();
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN','PHARMACIST')")
    public List<MedicineBatch> expiring(@RequestParam(defaultValue = "30") int withinDays) {
        return medicineService.expiringSoon(withinDays);
    }
}
