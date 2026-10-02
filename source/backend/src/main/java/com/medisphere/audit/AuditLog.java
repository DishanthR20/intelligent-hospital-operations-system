package com.medisphere.audit;

import com.medisphere.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @Column(nullable = false)
    private UUID actorUserId;

    @Column(nullable = false)
    private String actorRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuditAction action;

    private String entityType;
    private String entityId;

    @Column(nullable = false)
    private boolean success;

    private String reason;

    protected AuditLog() {
    }

    public AuditLog(UUID actorUserId, String actorRole, AuditAction action, String entityType,
                     String entityId, boolean success, String reason) {
        this.actorUserId = actorUserId;
        this.actorRole = actorRole;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.success = success;
        this.reason = reason;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public String getActorRole() {
        return actorRole;
    }

    public AuditAction getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public String getEntityId() {
        return entityId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getReason() {
        return reason;
    }
}
