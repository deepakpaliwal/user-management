package com.uums.api.auth.mfa;

import com.uums.api.auth.AuthException;
import com.uums.api.auth.dto.AuthResponse;
import com.uums.api.auth.dto.mfa.MfaChallengeRequest;
import com.uums.api.auth.dto.mfa.MfaChallengeResponse;
import com.uums.api.auth.dto.mfa.MfaVerifyRequest;
import com.uums.api.domain.User;
import com.uums.api.service.AuthService;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class MfaService {

    private final AuthService authService;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, MfaChallenge> challenges = new ConcurrentHashMap<>();
    private final long challengeTtlSeconds;

    public MfaService(AuthService authService, @Value("${uums.security.mfa.challenge-ttl-seconds:300}") long challengeTtlSeconds) {
        this.authService = authService;
        this.challengeTtlSeconds = challengeTtlSeconds;
    }

    public MfaChallengeResponse initiateChallenge(MfaChallengeRequest request) {
        User user = authService.validatePrimaryCredentials(request.username(), request.password());
        String otp = String.format("%06d", random.nextInt(1_000_000));
        String challengeId = UUID.randomUUID().toString();
        challenges.put(challengeId, new MfaChallenge(user.getUsername(), otp, Instant.now().plusSeconds(challengeTtlSeconds)));

        return new MfaChallengeResponse(
                challengeId,
                "EMAIL",
                maskEmail(user.getEmail()),
                challengeTtlSeconds,
                otp);
    }

    public AuthResponse verifyChallenge(MfaVerifyRequest request) {
        MfaChallenge challenge = challenges.get(request.challengeId());
        if (challenge == null || Instant.now().isAfter(challenge.expiresAt())) {
            throw new AuthException("MFA challenge expired or invalid");
        }
        if (!challenge.otp().equals(request.otp())) {
            throw new AuthException("Invalid OTP");
        }

        challenges.remove(request.challengeId());
        User user = authService.getActiveUserByUsername(challenge.username());
        return authService.issueTokens(user);
    }

    private String maskEmail(String email) {
        String[] parts = email.split("@", 2);
        if (parts.length != 2 || parts[0].length() < 2) {
            return "***";
        }
        return parts[0].charAt(0) + "***@" + parts[1];
    }

    private record MfaChallenge(String username, String otp, Instant expiresAt) {
    }
}
