package com.uums.api.auth.dto;

import java.util.Set;

public record AuthResponse(String accessToken, long expiresInSeconds, String tokenType, Set<String> roles) {
}
