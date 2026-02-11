package com.uums.api.auth.recovery;

import com.uums.api.auth.AuthException;
import com.uums.api.auth.dto.recovery.RecoveryChallengeRequest;
import com.uums.api.auth.dto.recovery.RecoveryChallengeResponse;
import com.uums.api.auth.dto.recovery.RecoveryResetRequest;
import com.uums.api.auth.dto.recovery.RecoverySetupRequest;
import com.uums.api.domain.User;
import com.uums.api.repository.UserRepository;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AccountRecoveryService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();
    private final Map<String, RecoveryChallenge> challenges = new ConcurrentHashMap<>();
    private final long challengeTtlSeconds;

    public AccountRecoveryService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${uums.security.recovery.challenge-ttl-seconds:300}") long challengeTtlSeconds) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.challengeTtlSeconds = challengeTtlSeconds;
    }

    @Transactional
    public void setupSecurityQuestion(RecoverySetupRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthException("User not found"));
        user.setSecurityQuestion(request.securityQuestion());
        user.setSecurityAnswerHash(passwordEncoder.encode(request.securityAnswer()));
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public RecoveryChallengeResponse initiateRecovery(RecoveryChallengeRequest request) {
        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new AuthException("User not found"));

        if (user.getSecurityQuestion() == null || user.getSecurityAnswerHash() == null) {
            throw new AuthException("Security question not configured");
        }

        if (!passwordEncoder.matches(request.securityAnswer(), user.getSecurityAnswerHash())) {
            throw new AuthException("Security answer is incorrect");
        }

        String otp = String.format("%06d", random.nextInt(1_000_000));
        String challengeId = UUID.randomUUID().toString();
        challenges.put(challengeId, new RecoveryChallenge(user.getUsername(), otp, Instant.now().plusSeconds(challengeTtlSeconds)));

        return new RecoveryChallengeResponse(
                challengeId,
                "EMAIL",
                maskEmail(user.getEmail()),
                user.getSecurityQuestion(),
                challengeTtlSeconds,
                otp);
    }

    @Transactional
    public void resetPassword(RecoveryResetRequest request) {
        RecoveryChallenge challenge = challenges.get(request.challengeId());
        if (challenge == null || Instant.now().isAfter(challenge.expiresAt())) {
            throw new AuthException("Recovery challenge expired or invalid");
        }
        if (!challenge.otp().equals(request.otp())) {
            throw new AuthException("Invalid OTP");
        }

        User user = userRepository.findByUsername(challenge.username())
                .orElseThrow(() -> new AuthException("User not found"));
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        challenges.remove(request.challengeId());
    }

    private String maskEmail(String email) {
        String[] parts = email.split("@", 2);
        if (parts.length != 2 || parts[0].length() < 2) {
            return "***";
        }
        return parts[0].charAt(0) + "***@" + parts[1];
    }

    private record RecoveryChallenge(String username, String otp, Instant expiresAt) {
    }
}
