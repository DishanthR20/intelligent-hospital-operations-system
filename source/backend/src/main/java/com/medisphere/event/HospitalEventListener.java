package com.medisphere.event;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Records every domain event onto the operations timeline. Runs after the
 * triggering transaction commits so the timeline never shows an action that
 * was rolled back.
 */
@Component
public class HospitalEventListener {

    private final HospitalEventRepository repository;

    public HospitalEventListener(HospitalEventRepository repository) {
        this.repository = repository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEvent(DomainEvent event) {
        repository.save(new HospitalEvent(event.getClass().getSimpleName(), event.summary(), event.patientId()));
    }
}
