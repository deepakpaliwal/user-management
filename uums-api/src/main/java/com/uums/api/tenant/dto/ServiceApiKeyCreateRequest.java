package com.uums.api.tenant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ServiceApiKeyCreateRequest(
        @NotBlank @Size(min = 2, max = 120) String keyName) {
}
