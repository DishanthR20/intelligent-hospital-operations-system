package com.medisphere.audit;

import com.medisphere.security.CurrentUser;
import com.medisphere.user.User;
import org.springframework.stereotype.Service;

/**
 * Records sensitive operations for the admin Audit Explorer. Called explicitly
 * from services at the point of the sensitive action, rather than via a blanket
 * AOP interceptor, so each call site can supply a meaningful entity reference.
 */
@Service
public class AuditService {

    private final AuditLogRepository repository;

    public AuditService(AuditLogRepository repository) {
        this.repository = repository;
    }

    public void record(AuditAction action, String entityType, String entityId) {
        record(action, entityType, entityId, true, null);
    }

    public void record(AuditAction action, String entityType, String entityId, boolean success, String reason) {
        User actor = CurrentUser.get();
        repository.save(new AuditLog(actor.getId(), actor.getRole().name(), action, entityType, entityId, success, reason));
    }
}
