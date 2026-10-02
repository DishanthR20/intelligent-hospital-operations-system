package com.medisphere.notification;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    public void notify(UUID recipientUserId, String message, Notification.Priority priority) {
        if (recipientUserId == null) return;
        repository.save(new Notification(recipientUserId, message, priority));
    }

    public List<Notification> forUser(UUID userId) {
        return repository.findByRecipientUserIdOrderByCreatedAtDesc(userId);
    }

    public long unreadCount(UUID userId) {
        return repository.countByRecipientUserIdAndReadFalse(userId);
    }

    public void markRead(UUID notificationId) {
        repository.findById(notificationId).ifPresent(n -> {
            n.markRead();
            repository.save(n);
        });
    }
}
