package com.medisphere.bed;

import com.medisphere.audit.AuditAction;
import com.medisphere.audit.AuditService;
import com.medisphere.bed.dto.BedResponse;
import com.medisphere.common.exception.ConflictException;
import com.medisphere.common.exception.ResourceNotFoundException;
import com.medisphere.event.BedReleasedEvent;
import com.medisphere.intelligence.DecisionRecorder;
import com.medisphere.intelligence.DecisionType;
import java.util.List;
import java.util.UUID;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BedService {

    private final BedRepository bedRepository;
    private final WardRepository wardRepository;
    private final BedAllocationEngine allocationEngine;
    private final DecisionRecorder decisionRecorder;
    private final AuditService auditService;
    private final ApplicationEventPublisher eventPublisher;

    public BedService(BedRepository bedRepository, WardRepository wardRepository, BedAllocationEngine allocationEngine,
                       DecisionRecorder decisionRecorder, AuditService auditService, ApplicationEventPublisher eventPublisher) {
        this.bedRepository = bedRepository;
        this.wardRepository = wardRepository;
        this.allocationEngine = allocationEngine;
        this.decisionRecorder = decisionRecorder;
        this.auditService = auditService;
        this.eventPublisher = eventPublisher;
    }

    public Ward createWard(String name, boolean icu, boolean isolation, String genderPolicy) {
        return wardRepository.save(new Ward(name, icu, isolation, genderPolicy));
    }

    public BedResponse createBed(UUID wardId, String bedNumber) {
        Ward ward = wardRepository.findById(wardId).orElseThrow(() -> ResourceNotFoundException.of("Ward", wardId));
        return BedResponse.from(bedRepository.save(new Bed(ward, bedNumber)));
    }

    @Transactional(readOnly = true)
    public List<Ward> listWards() {
        return wardRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<BedResponse> bedsInWard(UUID wardId, BedStatus status) {
        List<Bed> beds = status != null ? bedRepository.findByWard_IdAndStatus(wardId, status) : bedRepository.findByWard_Id(wardId);
        return beds.stream().map(BedResponse::from).toList();
    }

    public BedResponse allocate(UUID patientId, boolean needsIcu, boolean needsIsolation, String genderPolicy) {
        var allocation = allocationEngine.allocate(new BedAllocationEngine.Requirement(needsIcu, needsIsolation, genderPolicy));
        if (allocation.isEmpty()) {
            throw new ConflictException("NO_BED_AVAILABLE", "No bed currently matches these requirements");
        }
        Bed bed = allocation.get().bed();
        bed.occupy(patientId);
        decisionRecorder.record(DecisionType.BED_ALLOCATION, patientId, allocation.get().explanation());
        auditService.record(AuditAction.BED_ASSIGNMENT, "Bed", bed.getId().toString());
        return BedResponse.from(bed);
    }

    public void release(UUID bedId) {
        Bed bed = getEntity(bedId);
        bed.release();
        eventPublisher.publishEvent(new BedReleasedEvent(bed.getId(), bed.getBedNumber(), bed.getWard().getName()));
    }

    public void finishCleaning(UUID bedId) {
        getEntity(bedId).finishCleaning();
    }

    public void passInspection(UUID bedId) {
        getEntity(bedId).passInspection();
    }

    @Transactional(readOnly = true)
    public Bed getEntity(UUID id) {
        return bedRepository.findById(id).orElseThrow(() -> ResourceNotFoundException.of("Bed", id));
    }

    @Transactional(readOnly = true)
    public long occupancyPercent(UUID wardId) {
        long total = bedRepository.countByWard_Id(wardId);
        if (total == 0) return 0;
        long occupied = bedRepository.countByWard_IdAndStatus(wardId, BedStatus.OCCUPIED);
        return Math.round(occupied * 100.0 / total);
    }
}
