package com.medisphere.pharmacy;

import com.medisphere.common.exception.ConflictException;
import com.medisphere.common.exception.ResourceNotFoundException;
import com.medisphere.pharmacy.dto.AddBatchRequest;
import com.medisphere.pharmacy.dto.MedicineRequest;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final MedicineBatchRepository batchRepository;
    private final MedicineDemandEngine demandEngine;

    public MedicineService(MedicineRepository medicineRepository, MedicineBatchRepository batchRepository,
                            MedicineDemandEngine demandEngine) {
        this.medicineRepository = medicineRepository;
        this.batchRepository = batchRepository;
        this.demandEngine = demandEngine;
    }

    public Medicine create(MedicineRequest req) {
        if (medicineRepository.findByNameIgnoreCase(req.name()).isPresent()) {
            throw new ConflictException("MEDICINE_EXISTS", "A medicine with this name already exists");
        }
        return medicineRepository.save(new Medicine(req.name(), req.genericName(), req.unit(),
                req.pricePerUnit(), req.reorderThreshold()));
    }

    @Transactional(readOnly = true)
    public Page<Medicine> search(String q, Pageable pageable) {
        return medicineRepository.findByNameContainingIgnoreCase(q == null ? "" : q, pageable);
    }

    @Transactional(readOnly = true)
    public Medicine getEntity(UUID id) {
        return medicineRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Medicine", id));
    }

    public MedicineBatch addBatch(UUID medicineId, AddBatchRequest req) {
        Medicine medicine = getEntity(medicineId);
        return batchRepository.save(new MedicineBatch(medicine, req.batchNumber(), req.expiryDate(), req.supplier(), req.quantity()));
    }

    @Transactional(readOnly = true)
    public List<MedicineBatch> batchesFor(UUID medicineId) {
        return batchRepository.findByMedicine_IdOrderByExpiryDateAsc(medicineId);
    }

    @Transactional(readOnly = true)
    public List<MedicineDemandReport> demandReport() {
        return medicineRepository.findAll().stream().map(demandEngine::analyze).toList();
    }

    @Transactional(readOnly = true)
    public List<MedicineBatch> expiringSoon(int withinDays) {
        return batchRepository.findByExpiryDateBefore(java.time.LocalDate.now().plusDays(withinDays));
    }
}
