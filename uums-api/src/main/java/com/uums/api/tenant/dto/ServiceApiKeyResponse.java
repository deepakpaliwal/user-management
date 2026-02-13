package com.uums.api.tenant.dto;

import java.time.LocalDateTime;

public record ServiceApiKeyResponse(
        Long id,
        Long serviceId,
        String keyName,
        String apiKey,
        boolean active,
        LocalDateTime createdAt) {
}
