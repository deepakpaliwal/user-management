package com.uums.api.admin;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.uums.api.admin.dto.AdminUpdateUserRequest;
import com.uums.api.auth.AuthException;
import com.uums.api.domain.AccountStatus;
import com.uums.api.domain.Role;
import com.uums.api.domain.User;
import com.uums.api.repository.RoleRepository;
import com.uums.api.repository.UserRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AdminUserService adminUserService;

    @BeforeEach
    void setUp() {
        adminUserService = new AdminUserService(userRepository, roleRepository, passwordEncoder);
    }

    @Test
    void updateUserShouldApplyStatusPasswordAndRoles() {
        User user = new User();
        user.setUsername("test_user_01");
        user.setEmail("test@uums.local");
        user.setAccountStatus(AccountStatus.ACTIVE);

        Role admin = new Role();
        admin.setRoleCode("ROLE_ADMIN");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("StrongOverride@123")).thenReturn("encoded");
        when(roleRepository.findByRoleCode("ROLE_ADMIN")).thenReturn(Optional.of(admin));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        var request = new AdminUpdateUserRequest(AccountStatus.DISABLED, "StrongOverride@123", Set.of("ROLE_ADMIN"));
        var response = adminUserService.updateUser(1L, request);

        assertEquals(AccountStatus.DISABLED, response.accountStatus());
        assertEquals(Set.of("ROLE_ADMIN"), response.roles());
        assertEquals(0, response.failedLoginAttempts());
    }

    @Test
    void deleteUserShouldFailWhenMissing() {
        when(userRepository.existsById(99L)).thenReturn(false);
        assertThrows(AuthException.class, () -> adminUserService.deleteUser(99L));
    }
}
