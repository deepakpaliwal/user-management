package com.uums.api.auth.dto.recovery;

import jakarta.validation.constraints.NotBlank;

public record RecoverySetupRequest(
        @NotBlank String username,
        @NotBlank String securityQuestion,
        @NotBlank String securityAnswer) {
}
