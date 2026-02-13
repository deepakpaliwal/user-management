package com.uums.api.config.logging;

import java.util.Arrays;
import java.util.regex.Pattern;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RestEndpointLoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(RestEndpointLoggingAspect.class);
    private static final Pattern PASSWORD_EQUALS_PATTERN =
            Pattern.compile("(?i)(password|newPassword|securityAnswer|otp)=([^,)}\\]]+)");
    private static final Pattern PASSWORD_JSON_PATTERN =
            Pattern.compile("(?i)\\\"(password|newPassword|securityAnswer|otp)\\\"\\s*:\\s*\\\"[^\\\"]*\\\"");

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logEndpoint(ProceedingJoinPoint joinPoint) throws Throwable {
        String endpoint = joinPoint.getSignature().toShortString();
        String sanitizedArgs = sanitizeArgs(joinPoint.getArgs());

        log.info("REST ENTRY {} args={}", endpoint, sanitizedArgs);
        long startNs = System.nanoTime();

        try {
            Object result = joinPoint.proceed();
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
            log.info("REST EXIT {} elapsedMs={} resultType={}",
                    endpoint,
                    elapsedMs,
                    result == null ? "void" : result.getClass().getSimpleName());
            return result;
        } catch (Exception ex) {
            long elapsedMs = (System.nanoTime() - startNs) / 1_000_000;
            log.error("REST FAILURE {} elapsedMs={} error={}", endpoint, elapsedMs, ex.getMessage(), ex);
            throw ex;
        }
    }

    private String sanitizeArgs(Object[] args) {
        String raw = Arrays.toString(args);
        String sanitized = PASSWORD_EQUALS_PATTERN.matcher(raw).replaceAll("$1=***");
        return PASSWORD_JSON_PATTERN.matcher(sanitized).replaceAll("\\\"$1\\\":\\\"***\\\"");
    }
}
