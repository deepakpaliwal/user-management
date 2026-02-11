package com.uums.api.service;

import com.uums.api.auth.AuthException;
import com.uums.api.auth.dto.AuthResponse;
import com.uums.api.auth.dto.LoginRequest;
import com.uums.api.auth.dto.RefreshTokenRequest;
import com.uums.api.auth.dto.RegisterRequest;
import com.uums.api.domain.AccountStatus;
import com.uums.api.domain.Role;
import com.uums.api.domain.User;
import com.uums.api.repository.RoleRepository;
import com.uums.api.repository.UserRepository;
import com.uums.api.security.JwtService;
import io.jsonwebtoken.JwtException;
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
        User user = validatePrimaryCredentials(request.username(), request.password());
        return buildTokenResponse(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse refresh(RefreshTokenRequest request) {
        try {
            String username = jwtService.extractSubjectFromRefreshToken(request.refreshToken());
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new AuthException("Invalid refresh token"));
            if (user.getAccountStatus() != AccountStatus.ACTIVE) {
                throw new AuthException("Account is not active");
            }
            return buildTokenResponse(user);
        } catch (JwtException ex) {
            throw new AuthException("Invalid refresh token");
        }
    }

    @Transactional
    public User validatePrimaryCredentials(String username, String password) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("Invalid username or password"));

        if (user.getAccountStatus() == AccountStatus.LOCKED || user.getAccountStatus() == AccountStatus.DISABLED) {
            throw new AuthException("Account is not active");
        }

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= maxFailedAttempts) {
                user.setAccountStatus(AccountStatus.LOCKED);
            }
            userRepository.save(user);
            throw new AuthException("Invalid username or password");
        }

        user.setFailedLoginAttempts(0);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getActiveUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("Invalid user"));
        if (user.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AuthException("Account is not active");
        }
        return user;
    }

    public AuthResponse issueTokens(User user) {
        return buildTokenResponse(user);
    }

    private AuthResponse buildTokenResponse(User user) {
        Set<String> roles = user.getRoles().stream().map(Role::getRoleCode).collect(Collectors.toSet());
        String accessToken = jwtService.generateAccessToken(user.getUsername(), Map.of("roles", roles));
        String refreshToken = jwtService.generateRefreshToken(user.getUsername());
        return new AuthResponse(accessToken, refreshToken, jwtService.getAccessExpirationSeconds(), "Bearer", roles);
    }
}
