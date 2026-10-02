package com.medisphere.queue;

import com.medisphere.configuration.ConfigService;
import com.medisphere.intelligence.DecisionExplanation;
import com.medisphere.intelligence.DecisionFactor;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import org.springframework.stereotype.Component;

/**
 * Smart Queue Optimization (spec section 12). A configurable, fully explainable
 * scoring function ranked with a {@link PriorityQueue} (max-heap on score).
 *
 * Priority Score = Emergency Score + Clinical Risk + Waiting Time (aging) + Vulnerability
 *
 * Waiting-time aging means every extra minute in the queue adds points, so a
 * routine case that has waited long enough will always eventually rise above a
 * freshly-arrived routine case — nobody starves at the bottom forever.
 * There is deliberately no "VIP" factor anywhere in this formula: status can
 * never outrank clinical emergency, as required by hospital policy.
 */
@Component
public class QueuePriorityEngine {

    private final ConfigService configService;

    public QueuePriorityEngine(ConfigService configService) {
        this.configService = configService;
    }

    public List<ScoredQueueEntry> rank(List<QueueEntry> waiting) {
        PriorityQueue<ScoredQueueEntry> heap = new PriorityQueue<>(
                Comparator.comparingDouble(ScoredQueueEntry::score).reversed());
        for (QueueEntry entry : waiting) {
            heap.add(new ScoredQueueEntry(entry, explain(entry)));
        }
        List<ScoredQueueEntry> ordered = new ArrayList<>();
        while (!heap.isEmpty()) {
            ordered.add(heap.poll());
        }
        return ordered;
    }

    public DecisionExplanation explain(QueueEntry entry) {
        double emergencyWeight = configService.getDouble("queue.weight.emergency", 60);
        double clinicalRiskWeight = configService.getDouble("queue.weight.clinicalRiskPerPoint", 4);
        double waitingWeightPerMinute = configService.getDouble("queue.weight.waitingTimePerMinute", 0.5);
        double elderlyBonus = configService.getDouble("queue.weight.vulnerability.elderly", 10);
        double childBonus = configService.getDouble("queue.weight.vulnerability.child", 8);
        int elderlyAge = configService.getInt("queue.vulnerability.elderlyAge", 65);
        int childAge = configService.getInt("queue.vulnerability.childAge", 5);

        List<DecisionFactor> factors = new ArrayList<>();

        if (entry.isEmergency()) {
            factors.add(new DecisionFactor("Emergency", emergencyWeight, "Marked as an emergency case at triage"));
        }

        if (entry.getClinicalRisk() > 0) {
            double points = entry.getClinicalRisk() * clinicalRiskWeight;
            factors.add(new DecisionFactor("Clinical Risk", points,
                    "Triage clinical risk score " + entry.getClinicalRisk() + "/10"));
        }

        long waited = entry.waitingMinutes();
        if (waited > 0) {
            factors.add(new DecisionFactor("Waiting Time", waited * waitingWeightPerMinute,
                    "Waited " + waited + " minute(s); score rises the longer a patient waits so no one is stuck at the bottom"));
        }

        int age = entry.getPatient().getAge();
        if (age >= elderlyAge) {
            factors.add(new DecisionFactor("Vulnerability (Elderly)", elderlyBonus, "Patient age " + age + " (elderly policy)"));
        } else if (age <= childAge) {
            factors.add(new DecisionFactor("Vulnerability (Child)", childBonus, "Patient age " + age + " (child policy)"));
        }

        if (factors.isEmpty()) {
            factors.add(new DecisionFactor("Routine", 0, "No urgency factors currently apply"));
        }

        return new DecisionExplanation(entry.getPatient().getUser().getFullName(), factors);
    }
}
