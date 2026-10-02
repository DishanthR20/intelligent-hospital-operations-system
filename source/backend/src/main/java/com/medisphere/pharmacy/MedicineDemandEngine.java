package com.medisphere.pharmacy;

import com.medisphere.configuration.ConfigService;
import java.time.Duration;
import java.time.Instant;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Medicine Demand Engine (spec section 26): a Java statistical projection, not a
 * machine-learning forecast. Average daily usage is the real, observed
 * consumption rate (total units dispensed since tracking began, divided by the
 * number of days elapsed) — never a guess.
 */
@Component
@Transactional(readOnly = true)
public class MedicineDemandEngine {

    private final MedicineBatchRepository batchRepository;
    private final ConfigService configService;

    public MedicineDemandEngine(MedicineBatchRepository batchRepository, ConfigService configService) {
        this.batchRepository = batchRepository;
        this.configService = configService;
    }

    public MedicineDemandReport analyze(Medicine medicine) {
        int currentStock = batchRepository.findByMedicine_IdOrderByExpiryDateAsc(medicine.getId()).stream()
                .filter(b -> !b.isExpired())
                .mapToInt(MedicineBatch::getQuantity)
                .sum();

        long daysTracked = Math.max(1, Duration.between(medicine.getTrackingStartedAt(), Instant.now()).toDays());
        double averageDailyUsage = medicine.getTotalDispensed() / (double) daysTracked;

        Double estimatedRemainingDays = averageDailyUsage > 0 ? currentStock / averageDailyUsage : null;
        int reorderDaysThreshold = configService.getInt("pharmacy.reorderDaysThreshold", 7);
        int urgentDaysThreshold = configService.getInt("pharmacy.urgentReorderDaysThreshold", 3);

        ReorderStatus status;
        String explanation;
        if (currentStock <= medicine.getReorderThreshold()
                || (estimatedRemainingDays != null && estimatedRemainingDays <= urgentDaysThreshold)) {
            status = ReorderStatus.URGENT_REORDER;
            explanation = "Stock (" + currentStock + ") is at or below the reorder threshold ("
                    + medicine.getReorderThreshold() + "), or under " + urgentDaysThreshold + " days of supply remain.";
        } else if (estimatedRemainingDays != null && estimatedRemainingDays <= reorderDaysThreshold) {
            status = ReorderStatus.REORDER_RECOMMENDED;
            explanation = String.format("At the current usage rate of %.1f units/day, stock will last about %.1f more day(s).",
                    averageDailyUsage, estimatedRemainingDays);
        } else {
            status = ReorderStatus.OK;
            explanation = "Stock level is healthy relative to observed usage.";
        }

        return new MedicineDemandReport(medicine.getId(), medicine.getName(), currentStock, averageDailyUsage,
                estimatedRemainingDays, status, explanation);
    }
}
