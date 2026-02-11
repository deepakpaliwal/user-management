package com.uums.api.service;

import com.uums.api.auth.AuthException;
import com.uums.api.auth.dto.AuthResponse;
import com.uums.api.auth.dto.LoginRequest;
import com.uums.api.auth.dto.RegisterRequest;
import com.uums.api.domain.AccountStatus;
import com.uums.api.domain.Role;
import com.uums.api.domain.User;
import com.uums.api.repository.RoleRepository;
import com.uums.api.repository.UserRepository;
import com.uums.api.security.JwtService;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final int maxFailedAttempts;

    public AuthService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            @Value("${uums.security.max-failed-attempts:5}") int maxFailedAttempts) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.maxFailedAttempts = maxFailedAttempts;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new AuthException("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new AuthException("Email already exists");
        }

        Role userRole = roleRepository.findByRoleCode("ROLE_USER")
                .orElseThrow(() -> new AuthException("Default ROLE_USER not configured"));

        User user = new User();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.getRoles().add(userRole);

        User savedUser = userRepository.save(user);
        return buildTokenResponse(savedUser);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthException("Invalid username or password"));

        if (user.getAccountStatus() == AccountStatus.LOCKED || user.getAccountStatus() == AccountStatus.DISABLED) {
            throw new AuthException("Account is not active");
        }

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
                user.setAccountStatus(AccountStatus.LOCKED);
            }
            userRepository.save(user);
            throw new AuthException("Invalid username or password");
        }

        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        return buildTokenResponse(user);
    }

    private AuthResponse buildTokenResponse(User user) {
        Set<String> roles = user.getRoles().stream().map(Role::getRoleCode).collect(Collectors.toSet());
        String token = jwtService.generateToken(user.getUsername(), Map.of("roles", roles));
        return new AuthResponse(token, jwtService.getExpirationSeconds(), "Bearer", roles);
    }
}
