package com.uums.api.tenant.dto;

import com.uums.api.tenant.PricingTier;
import java.time.LocalDateTime;

public record ServiceApplicationResponse(
        Long id,
        String serviceName,
        String ownerEmail,
        String apiKey,
        PricingTier pricingTier,
        int requestLimitPerMinute,
        boolean active,
        LocalDateTime createdAt) {
}
