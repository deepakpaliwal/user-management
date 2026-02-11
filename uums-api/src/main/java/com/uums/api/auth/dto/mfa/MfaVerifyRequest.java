package com.uums.api.auth.dto.mfa;

import jakarta.validation.constraints.NotBlank;

public record MfaVerifyRequest(@NotBlank String challengeId, @NotBlank String otp) {
}
