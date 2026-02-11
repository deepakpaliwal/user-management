package com.uums.api.admin.dto;

import com.uums.api.domain.AccountStatus;
import java.util.Set;

public record AdminUserSummaryResponse(
        Long id,
        String username,
        String email,
        AccountStatus accountStatus,
        int failedLoginAttempts,
        Set<String> roles) {
}
