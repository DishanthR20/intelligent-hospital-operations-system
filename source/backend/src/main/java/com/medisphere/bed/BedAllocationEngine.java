package com.medisphere.bed;

import com.medisphere.intelligence.DecisionExplanation;
import com.medisphere.intelligence.DecisionFactor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Smart Bed Allocation (spec section 24). Hard requirements (ICU, isolation,
 * ward gender policy) are filtered first — a non-ICU bed is never offered for an
 * ICU need — then remaining candidates are scored so the choice is explainable
 * rather than "first free bed found".
 */
@Component
@Transactional(readOnly = true)
public class BedAllocationEngine {

    private final BedRepository bedRepository;

    public BedAllocationEngine(BedRepository bedRepository) {
        this.bedRepository = bedRepository;
    }

    public record Requirement(boolean needsIcu, boolean needsIsolation, String genderPolicy) {
    }

    public record Allocation(Bed bed, DecisionExplanation explanation) {
    }

    public Optional<Allocation> allocate(Requirement requirement) {
        List<Bed> available = bedRepository.findByStatus(BedStatus.AVAILABLE);

        List<Bed> eligible = available.stream()
                .filter(b -> !requirement.needsIcu() || b.getWard().isIcu())
                .filter(b -> !requirement.needsIsolation() || b.getWard().isIsolation())
                .filter(b -> requirement.genderPolicy() == null
                        || b.getWard().getGenderPolicy() == null
                        || "ANY".equalsIgnoreCase(b.getWard().getGenderPolicy())
                        || b.getWard().getGenderPolicy().equalsIgnoreCase(requirement.genderPolicy()))
                .toList();

        if (eligible.isEmpty()) return Optional.empty();

        // Prefer the ward that matches most closely on non-required attributes too
        // (e.g. don't waste an ICU-isolation bed on a plain admission) and the bed
        // whose ward currently has the most free capacity, to spread load evenly.
        Bed chosen = eligible.stream()
                .min(Comparator.comparingInt(this::wardOvermatchPenalty)
                        .thenComparing(b -> -freeBedsInWard(b.getWard().getId())))
                .orElseThrow();

        return Optional.of(new Allocation(chosen, explain(chosen, requirement)));
    }

    private int wardOvermatchPenalty(Bed bed) {
        // Penalize using a "special" bed (ICU/isolation) when it wasn't actually required.
        int penalty = 0;
        if (bed.getWard().isIcu()) penalty++;
        if (bed.getWard().isIsolation()) penalty++;
        return penalty;
    }

    private long freeBedsInWard(java.util.UUID wardId) {
        return bedRepository.countByWard_IdAndStatus(wardId, BedStatus.AVAILABLE);
    }

    private DecisionExplanation explain(Bed bed, Requirement requirement) {
        List<DecisionFactor> factors = new ArrayList<>();
        factors.add(new DecisionFactor("Ward", 0, "Assigned to ward " + bed.getWard().getName()));
        if (requirement.needsIcu()) {
            factors.add(new DecisionFactor("ICU Requirement", 10, "Bed is in an ICU-equipped ward"));
        }
        if (requirement.needsIsolation()) {
            factors.add(new DecisionFactor("Isolation Requirement", 10, "Bed is in an isolation ward"));
        }
        if (requirement.genderPolicy() != null && bed.getWard().getGenderPolicy() != null) {
            factors.add(new DecisionFactor("Ward Policy", 5,
                    "Ward gender policy (" + bed.getWard().getGenderPolicy() + ") is compatible"));
        }
        factors.add(new DecisionFactor("Load Balancing", 5,
                freeBedsInWard(bed.getWard().getId()) + " free bed(s) remain in this ward after allocation"));
        return new DecisionExplanation("Bed " + bed.getBedNumber() + " (" + bed.getWard().getName() + ")", factors);
    }
}
