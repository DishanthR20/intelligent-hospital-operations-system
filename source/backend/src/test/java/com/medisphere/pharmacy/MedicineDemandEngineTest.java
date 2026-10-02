package com.medisphere.pharmacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.medisphere.configuration.ConfigService;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Verifies the reorder-status math in the Medicine Demand Engine (spec section 26). */
@ExtendWith(MockitoExtension.class)
class MedicineDemandEngineTest {

    @Mock
    private MedicineBatchRepository batchRepository;
    @Mock
    private ConfigService configService;
    @InjectMocks
    private MedicineDemandEngine engine;

    private Medicine medicineTrackedForDays(int days, long totalDispensed, int reorderThreshold) {
        Medicine medicine = new Medicine("Amoxicillin", "Amoxicillin", "capsule", new BigDecimal("1.00"), reorderThreshold);
        medicine.recordDispensed(totalDispensed);
        // Simulate tracking having started `days` ago via reflection, since the field has no public setter.
        try {
            var field = Medicine.class.getDeclaredField("trackingStartedAt");
            field.setAccessible(true);
            field.set(medicine, Instant.now().minus(days, ChronoUnit.DAYS));
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
        return medicine;
    }

    private MedicineBatch batchOf(Medicine medicine, int quantity) {
        return new MedicineBatch(medicine, "B1", LocalDate.now().plusYears(1), "Supplier", quantity);
    }

    @Test
    void healthyStockIsReportedOk() {
        when(configService.getInt(anyString(), anyInt())).thenAnswer(inv -> inv.getArgument(1));
        Medicine medicine = medicineTrackedForDays(10, 20, 50); // 2 units/day usage
        when(batchRepository.findByMedicine_IdOrderByExpiryDateAsc(any()))
                .thenReturn(List.of(batchOf(medicine, 500))); // 250 days of supply

        MedicineDemandReport report = engine.analyze(medicine);

        assertThat(report.status()).isEqualTo(ReorderStatus.OK);
    }

    @Test
    void stockAtOrBelowThresholdIsUrgentRegardlessOfUsageRate() {
        when(configService.getInt(anyString(), anyInt())).thenAnswer(inv -> inv.getArgument(1));
        Medicine medicine = medicineTrackedForDays(10, 0, 50); // no usage yet, but...
        when(batchRepository.findByMedicine_IdOrderByExpiryDateAsc(any()))
                .thenReturn(List.of(batchOf(medicine, 40))); // ...stock is already under the threshold

        MedicineDemandReport report = engine.analyze(medicine);

        assertThat(report.status()).isEqualTo(ReorderStatus.URGENT_REORDER);
    }

    @Test
    void lowRemainingDaysTriggersReorderRecommendation() {
        when(configService.getInt(anyString(), anyInt())).thenAnswer(inv -> inv.getArgument(1)); // defaults: 7 / 3 days
        Medicine medicine = medicineTrackedForDays(10, 100, 20); // 10 units/day usage, threshold well below stock
        when(batchRepository.findByMedicine_IdOrderByExpiryDateAsc(any()))
                .thenReturn(List.of(batchOf(medicine, 50))); // 5 days of supply left -> recommend, not urgent

        MedicineDemandReport report = engine.analyze(medicine);

        assertThat(report.status()).isEqualTo(ReorderStatus.REORDER_RECOMMENDED);
        assertThat(report.estimatedRemainingDays()).isCloseTo(5.0, org.assertj.core.data.Offset.offset(0.5));
    }

    @Test
    void expiredBatchesAreExcludedFromCurrentStock() {
        when(configService.getInt(anyString(), anyInt())).thenAnswer(inv -> inv.getArgument(1));
        Medicine medicine = medicineTrackedForDays(10, 5, 10);
        MedicineBatch expired = new MedicineBatch(medicine, "OLD", LocalDate.now().minusDays(1), "Supplier", 1000);
        when(batchRepository.findByMedicine_IdOrderByExpiryDateAsc(any())).thenReturn(List.of(expired));

        MedicineDemandReport report = engine.analyze(medicine);

        assertThat(report.currentStock()).isZero();
    }
}
