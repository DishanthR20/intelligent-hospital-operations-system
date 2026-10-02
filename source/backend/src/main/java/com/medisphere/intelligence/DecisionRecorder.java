package com.medisphere.intelligence;

import java.util.UUID;
import org.springframework.stereotype.Service;

/** Thin convenience wrapper every engine calls to persist a decision for replay. */
@Service
public class DecisionRecorder {

    private final DecisionRepository repository;

    public DecisionRecorder(DecisionRepository repository) {
        this.repository = repository;
    }

    public Decision record(DecisionType type, UUID patientId, DecisionExplanation explanation) {
        return repository.save(Decision.record(type, patientId, explanation));
    }
}
