package com.medisphere.audit;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditLogRepository repository;

    public AuditController(AuditLogRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Page<AuditLog> list(@RequestParam(required = false) AuditAction action, Pageable pageable) {
        return action == null
                ? repository.findAllByOrderByCreatedAtDesc(pageable)
                : repository.findByActionOrderByCreatedAtDesc(action, pageable);
    }
}
