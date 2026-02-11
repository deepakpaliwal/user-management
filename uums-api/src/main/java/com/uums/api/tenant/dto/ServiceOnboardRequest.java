package com.uums.api.tenant.dto;

import com.uums.api.tenant.PricingTier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceOnboardRequest(
        @NotBlank @Size(min = 3, max = 160) String serviceName,
        @NotBlank @Email String ownerEmail,
        @NotNull PricingTier pricingTier) {
}
