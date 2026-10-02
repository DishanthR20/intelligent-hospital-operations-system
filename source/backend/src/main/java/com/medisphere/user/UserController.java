package com.medisphere.user;

import com.medisphere.user.dto.CreateStaffRequest;
import com.medisphere.user.dto.UserResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/staff")
    public UserResponse createStaff(@Valid @RequestBody CreateStaffRequest req) {
        return userService.createStaff(req);
    }

    @GetMapping
    public List<UserResponse> listByRole(@RequestParam Role role) {
        return userService.listByRole(role);
    }

    @PostMapping("/{id}/enable")
    public void enable(@PathVariable UUID id, @RequestBody Map<String, Boolean> body) {
        userService.setEnabled(id, Boolean.TRUE.equals(body.get("enabled")));
    }
}
