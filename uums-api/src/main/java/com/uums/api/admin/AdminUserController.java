package com.uums.api.admin;

import com.uums.api.admin.dto.AdminUpdateUserRequest;
import com.uums.api.admin.dto.AdminUserSummaryResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public List<AdminUserSummaryResponse> listUsers() {
        return adminUserService.listUsers();
    }

    @GetMapping("/{userId}")
    public AdminUserSummaryResponse getUser(@PathVariable Long userId) {
        return adminUserService.getUser(userId);
    }

    @PutMapping("/{userId}")
    public AdminUserSummaryResponse updateUser(@PathVariable Long userId, @Valid @RequestBody AdminUpdateUserRequest request) {
        return adminUserService.updateUser(userId, request);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
    }
}
