package com.medisphere.configuration;

import java.util.List;
import java.util.Map;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Admin Hospital Configuration Center (spec section 48). */
@RestController
@RequestMapping("/api/v1/configuration")
@PreAuthorize("hasRole('ADMIN')")
public class ConfigController {

    private final ConfigService configService;

    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    @GetMapping
    public List<HospitalConfiguration> all() {
        return configService.all();
    }

    @PutMapping
    public void update(@RequestBody Map<String, String> values) {
        configService.setAll(values);
    }
}
