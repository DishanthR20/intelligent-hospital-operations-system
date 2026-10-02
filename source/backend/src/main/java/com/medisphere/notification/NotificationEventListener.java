package com.medisphere.notification;

import com.medisphere.event.AppointmentCreatedEvent;
import com.medisphere.event.DischargeCompletedEvent;
import com.medisphere.event.LabResultCompletedEvent;
import com.medisphere.event.PatientArrivedEvent;
import com.medisphere.event.PaymentCompletedEvent;
import com.medisphere.event.PrescriptionCreatedEvent;
import com.medisphere.patient.PatientRepository;
import java.util.UUID;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/** Turns patient-facing domain events into in-app notifications for that patient's user account. */
@Component
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final PatientRepository patientRepository;

    public NotificationEventListener(NotificationService notificationService, PatientRepository patientRepository) {
        this.notificationService = notificationService;
        this.patientRepository = patientRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAppointmentCreated(AppointmentCreatedEvent event) {
        notify(event.patientId(), event.summary(), Notification.Priority.MEDIUM);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPatientArrived(PatientArrivedEvent event) {
        notify(event.patientId(), event.summary(), Notification.Priority.LOW);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLabResult(LabResultCompletedEvent event) {
        notify(event.patientId(), event.summary(),
                event.critical() ? Notification.Priority.CRITICAL : Notification.Priority.MEDIUM);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPrescriptionCreated(PrescriptionCreatedEvent event) {
        notify(event.patientId(), event.summary(), Notification.Priority.MEDIUM);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPaymentCompleted(PaymentCompletedEvent event) {
        notify(event.patientId(), event.summary(), Notification.Priority.LOW);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDischargeCompleted(DischargeCompletedEvent event) {
        notify(event.patientId(), event.summary(), Notification.Priority.HIGH);
    }

    private void notify(UUID patientId, String message, Notification.Priority priority) {
        if (patientId == null) return;
        patientRepository.findById(patientId)
                .ifPresent(p -> notificationService.notify(p.getUser().getId(), message, priority));
    }
}
