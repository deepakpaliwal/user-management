package com.uums.api.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private final SecretKey key;
    private final long accessExpirationSeconds;
    private final long refreshExpirationSeconds;

    public JwtService(
            @Value("${uums.security.jwt.secret}") String secret,
            @Value("${uums.security.jwt.expiration-seconds:3600}") long accessExpirationSeconds,
            @Value("${uums.security.jwt.refresh-expiration-seconds:1209600}") long refreshExpirationSeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpirationSeconds = accessExpirationSeconds;
        this.refreshExpirationSeconds = refreshExpirationSeconds;
    }

    public String generateAccessToken(String subject, Map<String, Object> claims) {
        return generateToken(subject, claims, accessExpirationSeconds, "access");
    }

    public String generateRefreshToken(String subject) {
        return generateToken(subject, Map.of(), refreshExpirationSeconds, "refresh");
    }

    private String generateToken(String subject, Map<String, Object> claims, long ttlSeconds, String tokenType) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claims(claims)
                .claim("typ", tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key)
                .compact();
    }

    public String extractSubjectFromRefreshToken(String refreshToken) {
        Claims claims = parse(refreshToken);
        Object type = claims.get("typ");
        if (!"refresh".equals(type)) {
            throw new JwtException("Invalid token type");
        }
        return claims.getSubject();
    }

    private Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public long getAccessExpirationSeconds() {
        return accessExpirationSeconds;
    }
}
