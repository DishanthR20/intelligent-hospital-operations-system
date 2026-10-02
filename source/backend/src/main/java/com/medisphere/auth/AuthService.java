package com.medisphere.auth;

import com.medisphere.audit.AuditAction;
import com.medisphere.audit.AuditLog;
import com.medisphere.audit.AuditLogRepository;
import com.medisphere.auth.dto.AuthResponse;
import com.medisphere.auth.dto.ChangePasswordRequest;
import com.medisphere.auth.dto.LoginRequest;
import com.medisphere.auth.dto.RegisterPatientRequest;
import com.medisphere.common.exception.BadRequestException;
import com.medisphere.common.exception.ConflictException;
import com.medisphere.event.PatientRegisteredEvent;
import com.medisphere.patient.Patient;
import com.medisphere.patient.PatientRepository;
import com.medisphere.security.CurrentUser;
import com.medisphere.security.JwtService;
import com.medisphere.user.Role;
import com.medisphere.user.User;
import com.medisphere.user.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final AuditLogRepository auditLogRepository;
    private final ApplicationEventPublisher eventPublisher;

    public AuthService(UserRepository userRepository, PatientRepository patientRepository,
                        PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager,
                        JwtService jwtService, AuditLogRepository auditLogRepository,
                        ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.auditLogRepository = auditLogRepository;
        this.eventPublisher = eventPublisher;
    }

    public AuthResponse login(LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password()));
        } catch (BadCredentialsException ex) {
            userRepository.findByEmailIgnoreCase(req.email()).ifPresent(u ->
                    auditLogRepository.save(new AuditLog(u.getId(), u.getRole().name(), AuditAction.LOGIN,
                            "User", u.getId().toString(), false, "Invalid credentials")));
            throw ex;
        }
        User user = userRepository.findByEmailIgnoreCase(req.email()).orElseThrow();
        auditLogRepository.save(new AuditLog(user.getId(), user.getRole().name(), AuditAction.LOGIN,
                "User", user.getId().toString(), true, null));
        return issueTokens(user);
    }

    public AuthResponse refresh(String refreshToken) {
        String email;
        try {
            email = jwtService.extractUsername(refreshToken);
        } catch (Exception ex) {
            throw new BadRequestException("INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired");
        }
        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new BadRequestException("INVALID_REFRESH_TOKEN", "Unknown account"));
        if (!jwtService.isRefreshToken(refreshToken) || !jwtService.isTokenValid(refreshToken, user)) {
            throw new BadRequestException("INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired");
        }
        return issueTokens(user);
    }

    public AuthResponse registerPatient(RegisterPatientRequest req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ConflictException("EMAIL_IN_USE", "An account already exists for this email");
        }
        User user = new User(req.email(), passwordEncoder.encode(req.password()), req.fullName(),
                req.phone(), Role.PATIENT);
        userRepository.save(user);

        Patient.Gender gender = Patient.Gender.valueOf(req.gender().toUpperCase());
        Patient patient = new Patient(user, req.dateOfBirth(), gender, req.bloodGroup());
        patient.setAddress(req.address());
        patient.setEmergencyContactName(req.emergencyContactName());
        patient.setEmergencyContactPhone(req.emergencyContactPhone());
        patientRepository.save(patient);

        eventPublisher.publishEvent(new PatientRegisteredEvent(patient.getId(), user.getFullName()));
        return issueTokens(user);
    }

    public void changePassword(ChangePasswordRequest req) {
        User user = userRepository.findById(CurrentUser.get().getId()).orElseThrow();
        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            throw new BadRequestException("INVALID_CURRENT_PASSWORD", "Current password is incorrect");
        }
        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }

    private AuthResponse issueTokens(User user) {
        String access = jwtService.generateAccessToken(user, java.util.Map.of("role", user.getRole().name(),
                "name", user.getFullName()));
        String refresh = jwtService.generateRefreshToken(user);
        return new AuthResponse(access, refresh, user.getId(), user.getFullName(), user.getEmail(), user.getRole());
    }
}
