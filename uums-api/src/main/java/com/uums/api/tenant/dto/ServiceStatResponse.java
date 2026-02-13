package com.uums.api.tenant.dto;

import java.time.LocalDateTime;

public record ServiceStatResponse(
        Long id,
        Long serviceId,
        String serviceName,
        String statCode,
        Long statValue,
        LocalDateTime updatedAt) {
}
