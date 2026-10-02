package com.medisphere.billing.dto;

import com.medisphere.billing.Invoice;
import com.medisphere.billing.InvoiceItem;
import com.medisphere.billing.Payment;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record InvoiceResponse(UUID id, UUID patientId, String status, List<Item> items, List<PaymentView> payments) {

    public record Item(String description, String category, BigDecimal unitPrice, int quantity) {
        static Item from(InvoiceItem i) {
            return new Item(i.getDescription(), i.getCategory().name(), i.getUnitPrice(), i.getQuantity());
        }
    }

    public record PaymentView(BigDecimal amount, String method) {
        static PaymentView from(Payment p) {
            return new PaymentView(p.getAmount(), p.getMethod().name());
        }
    }

    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(invoice.getId(), invoice.getPatient().getId(), invoice.getStatus().name(),
                invoice.getItems().stream().map(Item::from).toList(),
                invoice.getPayments().stream().map(PaymentView::from).toList());
    }
}
