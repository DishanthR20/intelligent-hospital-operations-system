package com.medisphere.notification;

import com.medisphere.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.util.UUID;

@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    public enum Priority { LOW, MEDIUM, HIGH, CRITICAL }

    @Column(nullable = false)
    private UUID recipientUserId;

    @Column(nullable = false, length = 500)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Priority priority;

    @Column(nullable = false)
    private boolean read = false;

    protected Notification() {
    }

    public Notification(UUID recipientUserId, String message, Priority priority) {
        this.recipientUserId = recipientUserId;
        this.message = message;
        this.priority = priority;
    }

    public UUID getRecipientUserId() {
        return recipientUserId;
    }

    public String getMessage() {
        return message;
    }

    public Priority getPriority() {
        return priority;
    }

    public boolean isRead() {
        return read;
    }

    public void markRead() {
        this.read = true;
    }
}
