package com.uums.api.auth.dto.recovery;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RecoveryResetRequest(
        @NotBlank String challengeId,
        @NotBlank String otp,
        @NotBlank
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{12,}$",
                message = "Password must be minimum 12 chars with upper, lower, number and special char")
        String newPassword) {
}
