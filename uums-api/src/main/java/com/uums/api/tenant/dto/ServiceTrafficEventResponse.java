package com.uums.api.tenant.dto;

import java.time.LocalDateTime;

public record ServiceTrafficEventResponse(
        Long id,
        Long serviceId,
        String serviceName,
        String apiName,
        String inputData,
        String status,
        LocalDateTime eventTime) {
}
