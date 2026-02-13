package com.uums.api.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceStatRequest(
        @NotBlank @Size(max = 80) String statCode,
        @NotNull Long statValue) {
}
