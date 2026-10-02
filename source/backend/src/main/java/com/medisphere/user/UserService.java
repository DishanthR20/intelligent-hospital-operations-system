package com.medisphere.user;

import com.medisphere.audit.AuditAction;
import com.medisphere.audit.AuditService;
import com.medisphere.common.exception.BadRequestException;
import com.medisphere.common.exception.ConflictException;
import com.medisphere.common.exception.ResourceNotFoundException;
import com.medisphere.doctor.DoctorService;
import com.medisphere.user.dto.CreateStaffRequest;
import com.medisphere.user.dto.UserResponse;
import java.util.List;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorService doctorService;
    private final AuditService auditService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        DoctorService doctorService, AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.doctorService = doctorService;
        this.auditService = auditService;
    }

    public UserResponse createStaff(CreateStaffRequest req) {
        if (req.role() == Role.PATIENT) {
            throw new BadRequestException("USE_PATIENT_REGISTRATION", "Patients self-register via /auth/register/patient");
        }
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new ConflictException("EMAIL_IN_USE", "An account already exists for this email");
        }
        User user = new User(req.email(), passwordEncoder.encode(req.password()), req.fullName(), req.phone(), req.role());
        userRepository.save(user);

        if (req.role() == Role.DOCTOR) {
            if (req.departmentId() == null || req.specialization() == null) {
                throw new BadRequestException("DOCTOR_PROFILE_INCOMPLETE",
                        "departmentId and specialization are required to onboard a doctor");
            }
            doctorService.createProfile(user, req.departmentId(), req.specialization(),
                    req.qualification(), req.experienceYears() == null ? 0 : req.experienceYears());
        }
        auditService.record(AuditAction.USER_PERMISSION_CHANGE, "User", user.getId().toString());
        return UserResponse.from(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> listByRole(Role role) {
        return userRepository.findByRole(role).stream().map(UserResponse::from).toList();
    }

    public void setEnabled(UUID userId, boolean enabled) {
        User user = userRepository.findById(userId).orElseThrow(() -> ResourceNotFoundException.of("User", userId));
        user.setEnabled(enabled);
        auditService.record(AuditAction.USER_PERMISSION_CHANGE, "User", userId.toString());
    }
}
