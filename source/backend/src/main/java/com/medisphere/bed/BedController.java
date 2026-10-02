package com.medisphere.bed;

import com.medisphere.bed.dto.AllocateBedRequest;
import com.medisphere.bed.dto.BedResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/beds")
public class BedController {

    private final BedService bedService;

    public BedController(BedService bedService) {
        this.bedService = bedService;
    }

    @GetMapping("/wards")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    public List<Ward> wards() {
        return bedService.listWards();
    }

    @PostMapping("/wards")
    @PreAuthorize("hasRole('ADMIN')")
    public Ward createWard(@RequestBody Map<String, Object> body) {
        return bedService.createWard((String) body.get("name"), Boolean.TRUE.equals(body.get("icu")),
                Boolean.TRUE.equals(body.get("isolation")), (String) body.get("genderPolicy"));
    }

    @PostMapping("/wards/{wardId}/beds")
    @PreAuthorize("hasRole('ADMIN')")
    public BedResponse createBed(@PathVariable UUID wardId, @RequestBody Map<String, String> body) {
        return bedService.createBed(wardId, body.get("bedNumber"));
    }

    @GetMapping("/wards/{wardId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    public List<BedResponse> bedsInWard(@PathVariable UUID wardId, @RequestParam(required = false) BedStatus status) {
        return bedService.bedsInWard(wardId, status);
    }

    @PostMapping("/allocate")
    @PreAuthorize("hasAnyRole('ADMIN','NURSE','DOCTOR')")
    public BedResponse allocate(@Valid @RequestBody AllocateBedRequest req) {
        return bedService.allocate(req.patientId(), req.needsIcu(), req.needsIsolation(), req.genderPolicy());
    }

    @PostMapping("/{id}/release")
    @PreAuthorize("hasAnyRole('ADMIN','NURSE')")
    public void release(@PathVariable UUID id) {
        bedService.release(id);
    }

    @PostMapping("/{id}/cleaning-complete")
    @PreAuthorize("hasAnyRole('ADMIN','NURSE')")
    public void finishCleaning(@PathVariable UUID id) {
        bedService.finishCleaning(id);
    }

    @PostMapping("/{id}/inspection-passed")
    @PreAuthorize("hasAnyRole('ADMIN','NURSE')")
    public void passInspection(@PathVariable UUID id) {
        bedService.passInspection(id);
    }
}
