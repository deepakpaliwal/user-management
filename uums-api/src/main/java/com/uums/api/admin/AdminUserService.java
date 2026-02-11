package com.uums.api.admin;

import com.uums.api.admin.dto.AdminUpdateUserRequest;
import com.uums.api.admin.dto.AdminUserSummaryResponse;
import com.uums.api.auth.AuthException;
import com.uums.api.domain.Role;
import com.uums.api.domain.User;
import com.uums.api.repository.RoleRepository;
import com.uums.api.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminUserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminUserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<AdminUserSummaryResponse> listUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public AdminUserSummaryResponse getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));
        return toResponse(user);
    }

    @Transactional
    public AdminUserSummaryResponse updateUser(Long userId, AdminUpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AuthException("User not found"));

        if (request.accountStatus() != null) {
            user.setAccountStatus(request.accountStatus());
        }

        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
            user.setFailedLoginAttempts(0);
        }

        if (request.roleCodes() != null && !request.roleCodes().isEmpty()) {
            Set<Role> roles = request.roleCodes().stream()
                    .map(code -> roleRepository.findByRoleCode(code)
                            .orElseThrow(() -> new AuthException("Role not found: " + code)))
                    .collect(Collectors.toSet());
            user.getRoles().clear();
            user.getRoles().addAll(roles);
        }

        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new AuthException("User not found");
        }
        userRepository.deleteById(userId);
    }

    private AdminUserSummaryResponse toResponse(User user) {
        return new AdminUserSummaryResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getAccountStatus(),
                user.getFailedLoginAttempts(),
                user.getRoles().stream().map(Role::getRoleCode).collect(Collectors.toSet()));
    }
}
