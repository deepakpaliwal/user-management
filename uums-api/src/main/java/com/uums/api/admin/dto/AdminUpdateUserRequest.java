package com.uums.api.admin.dto;

import com.uums.api.domain.AccountStatus;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record AdminUpdateUserRequest(
        AccountStatus accountStatus,
        @Size(min = 12, message = "Password override should be at least 12 characters") String password,
        Set<String> roleCodes) {
}
