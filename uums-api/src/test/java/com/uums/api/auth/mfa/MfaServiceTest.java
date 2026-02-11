package com.uums.api.auth.mfa;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import com.uums.api.auth.AuthException;
import com.uums.api.auth.dto.AuthResponse;
import com.uums.api.auth.dto.mfa.MfaChallengeRequest;
import com.uums.api.auth.dto.mfa.MfaVerifyRequest;
import com.uums.api.domain.AccountStatus;
import com.uums.api.domain.User;
import com.uums.api.service.AuthService;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {

    @Mock
    private AuthService authService;

    private MfaService mfaService;

    @BeforeEach
    void setUp() {
        mfaService = new MfaService(authService, 300);
    }

    @Test
    void challengeAndVerifyShouldIssueTokens() {
        User user = new User();
        user.setUsername("test_user_01");
        user.setEmail("test@uums.local");
        user.setAccountStatus(AccountStatus.ACTIVE);

        when(authService.validatePrimaryCredentials("test_user_01", "Password@123")).thenReturn(user);
        when(authService.getActiveUserByUsername("test_user_01")).thenReturn(user);
        when(authService.issueTokens(user)).thenReturn(new AuthResponse("access", "refresh", 3600, "Bearer", Set.of("ROLE_USER")));

        var challenge = mfaService.initiateChallenge(new MfaChallengeRequest("test_user_01", "Password@123"));
        var response = mfaService.verifyChallenge(new MfaVerifyRequest(challenge.challengeId(), challenge.debugOtp()));

        assertEquals("access", response.accessToken());
        assertEquals("refresh", response.refreshToken());
    }

    @Test
    void verifyShouldFailForInvalidChallenge() {
        assertThrows(AuthException.class, () -> mfaService.verifyChallenge(new MfaVerifyRequest("missing", "123456")));
    }
}
