package com.uums.api.auth.recovery;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.uums.api.auth.AuthException;
import com.uums.api.auth.dto.recovery.RecoveryChallengeRequest;
import com.uums.api.auth.dto.recovery.RecoveryResetRequest;
import com.uums.api.auth.dto.recovery.RecoverySetupRequest;
import com.uums.api.domain.User;
import com.uums.api.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AccountRecoveryServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private AccountRecoveryService accountRecoveryService;

    @BeforeEach
    void setUp() {
        accountRecoveryService = new AccountRecoveryService(userRepository, passwordEncoder, 300);
    }

    @Test
    void setupAndChallengeAndResetShouldSucceed() {
        User user = new User();
        user.setUsername("test_user_01");
        user.setEmail("test@uums.local");
        user.setSecurityQuestion("Pet name?");
        user.setSecurityAnswerHash("hashed-answer");

        when(userRepository.findByUsername("test_user_01")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("Shadow"))
                .thenReturn("hashed-answer")
                .thenReturn("new-password-hash");
        when(passwordEncoder.matches("Shadow", "hashed-answer")).thenReturn(true);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        accountRecoveryService.setupSecurityQuestion(new RecoverySetupRequest("test_user_01", "Pet name?", "Shadow"));
        var challenge = accountRecoveryService.initiateRecovery(new RecoveryChallengeRequest("test_user_01", "Shadow"));
        accountRecoveryService.resetPassword(new RecoveryResetRequest(challenge.challengeId(), challenge.debugOtp(), "NewStrong@1234"));

        assertEquals("Pet name?", challenge.securityQuestion());
    }

    @Test
    void initiateRecoveryShouldFailWithWrongAnswer() {
        User user = new User();
        user.setUsername("test_user_01");
        user.setEmail("test@uums.local");
        user.setSecurityQuestion("Pet name?");
        user.setSecurityAnswerHash("hashed-answer");

        when(userRepository.findByUsername("test_user_01")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Wrong", "hashed-answer")).thenReturn(false);

        assertThrows(AuthException.class,
                () -> accountRecoveryService.initiateRecovery(new RecoveryChallengeRequest("test_user_01", "Wrong")));
    }
}
