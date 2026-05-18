package com.example.demo.config;

import java.io.IOException;
import java.time.Duration;
import java.util.function.Supplier;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.dtos.responses.BaseResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisRateLimitingFilter extends OncePerRequestFilter {

    private static final String RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining";
    private static final String RATE_LIMIT_RETRY_AFTER = "Retry-After";
    private static final String RATE_LIMIT_RESET = "X-Rate-Limit-Reset";

    private final ProxyManager<String> proxyManager;
    private final Supplier<BucketConfiguration> bucketConfiguration;
    private final ObjectMapper objectMapper;

    @Value("${app.rate-limit.enabled:true}")
    private boolean enabled;

    @Value("${app.rate-limit.api-prefix:/api/v1}")
    private String apiPrefix;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }

        Bucket bucket = proxyManager.builder().build(buildRateLimitKey(request), bucketConfiguration);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        response.setHeader(RATE_LIMIT_REMAINING, String.valueOf(probe.getRemainingTokens()));

        if (probe.isConsumed()) {
            filterChain.doFilter(request, response);
            return;
        }

        long secondsToWait = Duration.ofNanos(probe.getNanosToWaitForRefill()).toSeconds();
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(RATE_LIMIT_RETRY_AFTER, String.valueOf(Math.max(1, secondsToWait)));
        response.setHeader(RATE_LIMIT_RESET, String.valueOf(Math.max(1, secondsToWait)));

        BaseResponse<Object> body = BaseResponse.error("Too many requests. Please retry later.", null);
        objectMapper.writeValue(response.getOutputStream(), body);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        if (path.startsWith("/v3/api-docs")
                || path.startsWith("/swagger-ui")
                || path.equals("/swagger-ui.html")) {
            return true;
        }
        return !path.startsWith(apiPrefix);
    }

    private String buildRateLimitKey(HttpServletRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identifier = request.getRemoteAddr();

        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
            identifier = authentication.getName();
        }

        return request.getMethod() + ":" + request.getServletPath() + ":" + identifier;
    }
}
