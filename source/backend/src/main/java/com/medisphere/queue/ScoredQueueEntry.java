package com.medisphere.queue;

import com.medisphere.intelligence.DecisionExplanation;

public record ScoredQueueEntry(QueueEntry entry, DecisionExplanation explanation) {
    public double score() {
        return explanation.totalScore();
    }
}
