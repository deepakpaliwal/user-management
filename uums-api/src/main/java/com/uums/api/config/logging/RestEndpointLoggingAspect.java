package com.uums.api.config.logging;

import java.util.Arrays;
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

    @Around("within(@org.springframework.web.bind.annotation.RestController *)")
    public Object logEndpoint(ProceedingJoinPoint joinPoint) throws Throwable {
        String endpoint = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        log.info("REST ENTRY {} args={}", endpoint, Arrays.toString(args));
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
}
