package com.uums.api.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public record TrafficIngestMessage(
        @NotNull Long serviceId,
        @NotBlank @Size(min = 2, max = 160) String apiName,
        @NotBlank @Size(max = 8000) String inputData,
        @NotBlank @Size(max = 40) String status,
        LocalDateTime eventTime) {
}
