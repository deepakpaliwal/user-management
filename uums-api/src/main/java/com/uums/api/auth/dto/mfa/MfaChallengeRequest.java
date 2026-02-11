package com.uums.api.auth.dto.mfa;

import jakarta.validation.constraints.NotBlank;

public record MfaChallengeRequest(@NotBlank String username, @NotBlank String password) {
}
