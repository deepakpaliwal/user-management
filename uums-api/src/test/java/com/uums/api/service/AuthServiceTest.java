package com.uums.api.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.uums.api.auth.AuthException;
import com.uums.api.auth.dto.LoginRequest;
import com.uums.api.auth.dto.RefreshTokenRequest;
import com.uums.api.auth.dto.RegisterRequest;
import com.uums.api.domain.AccountStatus;
import com.uums.api.domain.Role;
import com.uums.api.domain.User;
import com.uums.api.repository.RoleRepository;
import com.uums.api.repository.UserRepository;
import com.uums.api.security.JwtService;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private RoleRepository roleRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, roleRepository, passwordEncoder, jwtService, 3);
    }

    @Test
    void registerShouldAssignRoleUser() {
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@uums.local")).thenReturn(false);

        Role role = new Role();
        role.setRoleCode("ROLE_USER");
        role.setRoleName("Standard User");
        when(roleRepository.findByRoleCode("ROLE_USER")).thenReturn(Optional.of(role));

        when(passwordEncoder.encode("StrongPass@123")).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(jwtService.generateAccessToken(any(), any())).thenReturn("access-jwt-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("refresh-jwt-token");
        when(jwtService.getAccessExpirationSeconds()).thenReturn(3600L);

        var response = authService.register(new RegisterRequest("newuser", "new@uums.local", "StrongPass@123"));
        assertEquals("access-jwt-token", response.accessToken());
        assertEquals("refresh-jwt-token", response.refreshToken());
        assertEquals("Bearer", response.tokenType());
    }

    @Test
    void loginShouldLockAccountAfterThreshold() {
        User user = new User();
        user.setUsername("locked-user");
        user.setPasswordHash("encoded");
        user.setAccountStatus(AccountStatus.ACTIVE);
        user.setFailedLoginAttempts(2);

        when(userRepository.findByUsername("locked-user")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);

        assertThrows(AuthException.class, () -> authService.login(new LoginRequest("locked-user", "wrong")));
        assertEquals(AccountStatus.LOCKED, user.getAccountStatus());
        assertEquals(3, user.getFailedLoginAttempts());
    }

    @Test
    void refreshShouldIssueNewAccessAndRefreshTokens() {
        User user = new User();
        user.setUsername("test_user_01");
        user.setAccountStatus(AccountStatus.ACTIVE);

        Role role = new Role();
        role.setRoleCode("ROLE_USER");
        user.getRoles().add(role);

        when(jwtService.extractSubjectFromRefreshToken("valid-refresh-token")).thenReturn("test_user_01");
        when(userRepository.findByUsername("test_user_01")).thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(any(), any())).thenReturn("new-access-token");
        when(jwtService.generateRefreshToken(any())).thenReturn("new-refresh-token");
        when(jwtService.getAccessExpirationSeconds()).thenReturn(3600L);

        var response = authService.refresh(new RefreshTokenRequest("valid-refresh-token"));

        assertEquals("new-access-token", response.accessToken());
        assertEquals("new-refresh-token", response.refreshToken());
    }
}
