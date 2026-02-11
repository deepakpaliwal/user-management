package com.uums.api.auth.dto.mfa;

public record MfaChallengeResponse(
        String challengeId,
        String deliveryChannel,
        String maskedDestination,
        long expiresInSeconds,
        String debugOtp) {
}
