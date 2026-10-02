package com.medisphere.analytics;

import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final IntelligenceAlertEngine alertEngine;

    public AnalyticsController(AnalyticsService analyticsService, IntelligenceAlertEngine alertEngine) {
        this.analyticsService = analyticsService;
        this.alertEngine = alertEngine;
    }

    @GetMapping("/digital-twin")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','NURSE','RECEPTIONIST')")
    public List<DepartmentStatus> digitalTwin() {
        return analyticsService.digitalTwin();
    }

    @GetMapping("/command-center")
    @PreAuthorize("hasRole('ADMIN')")
    public HospitalMetrics commandCenter() {
        return analyticsService.commandCenterMetrics();
    }

    @GetMapping("/alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public List<String> alerts() {
        return alertEngine.generate();
    }
}
