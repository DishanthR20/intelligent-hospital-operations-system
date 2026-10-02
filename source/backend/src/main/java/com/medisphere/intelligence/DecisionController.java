package com.medisphere.intelligence;

import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Admin "Decision Replay" page: browse every explainable recommendation the platform has made. */
@RestController
@RequestMapping("/api/v1/decisions")
@PreAuthorize("hasRole('ADMIN')")
public class DecisionController {

    private final DecisionRepository repository;

    public DecisionController(DecisionRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public Page<Decision> list(@RequestParam(required = false) DecisionType type,
                                @RequestParam(required = false) UUID patientId,
                                Pageable pageable) {
        if (patientId != null) return repository.findByPatientIdOrderByCreatedAtDesc(patientId, pageable);
        if (type != null) return repository.findByTypeOrderByCreatedAtDesc(type, pageable);
        return repository.findAllByOrderByCreatedAtDesc(pageable);
    }
}
