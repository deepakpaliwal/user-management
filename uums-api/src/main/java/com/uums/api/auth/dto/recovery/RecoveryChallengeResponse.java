package com.uums.api.auth.dto.recovery;

public record RecoveryChallengeResponse(
        String challengeId,
        String deliveryChannel,
        String maskedDestination,
        String securityQuestion,
        long expiresInSeconds,
        String debugOtp) {
}
