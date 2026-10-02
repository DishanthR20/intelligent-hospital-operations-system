package com.medisphere.auth;

import com.medisphere.auth.dto.AuthResponse;
import com.medisphere.auth.dto.ChangePasswordRequest;
import com.medisphere.auth.dto.LoginRequest;
import com.medisphere.auth.dto.RefreshRequest;
import com.medisphere.auth.dto.RegisterPatientRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest req) {
        return authService.refresh(req.refreshToken());
    }

    @PostMapping("/register/patient")
    public ResponseEntity<AuthResponse> registerPatient(@Valid @RequestBody RegisterPatientRequest req) {
        return ResponseEntity.ok(authService.registerPatient(req));
    }

    @PostMapping("/change-password")
    public void changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(req);
    }
}
