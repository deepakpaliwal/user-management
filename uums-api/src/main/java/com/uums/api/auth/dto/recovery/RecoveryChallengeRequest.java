package com.uums.api.auth.dto.recovery;

import jakarta.validation.constraints.NotBlank;

public record RecoveryChallengeRequest(@NotBlank String username, @NotBlank String securityAnswer) {
}
