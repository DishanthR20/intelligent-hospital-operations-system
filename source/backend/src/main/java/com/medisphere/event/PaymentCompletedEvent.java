package com.medisphere.event;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentCompletedEvent(UUID patientId, UUID invoiceId, BigDecimal amount) implements DomainEvent {
    @Override
    public String summary() {
        return "Payment of " + amount + " received for invoice " + invoiceId;
    }
}
