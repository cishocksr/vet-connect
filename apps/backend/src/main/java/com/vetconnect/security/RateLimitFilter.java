package com.vetconnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting Filter
 *
 * Prevents abuse by limiting requests per IP address
 * Specifically protects authentication endpoints
 *
 * CONFIGURATION:
 * - Max 60 requests per minute per IP for auth endpoints
 * - Uses Redis for distributed rate limiting
 * - Returns 429 Too Many Requests when limit exceeded
 *
 * ORDER: Runs early in filter chain (Order = 1)
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 1)
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_REQUESTS_PER_MINUTE = 60;
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        String clientIp = getClientIP(request);
        String requestUri = request.getRequestURI();

        // Only rate limit authentication endpoints
        if (requestUri.startsWith("/api/auth/")) {
            String key = RATE_LIMIT_PREFIX + clientIp;

            try {
                // Increment request count
                Long requests = redisTemplate.opsForValue().increment(key);

                // Set expiry on first request
                if (requests != null && requests == 1) {
                    redisTemplate.expire(key, 1, TimeUnit.MINUTES);
                }

                // Check if limit exceeded
                if (requests != null && requests > MAX_REQUESTS_PER_MINUTE) {
                    log.warn("Rate limit exceeded for IP: {} (requests: {})",
                            clientIp, requests);

                    response.setStatus(429); // Too Many Requests
                    response.setContentType("application/json");
                    response.getWriter().write(
                            "{\"success\":false," +
                                    "\"message\":\"Too many requests. Please try again later.\"," +
                                    "\"error\":\"Rate limit exceeded\"}"
                    );
                    return;
                }

                log.debug("Rate limit check passed for IP: {} (requests: {}/{})",
                        clientIp, requests, MAX_REQUESTS_PER_MINUTE);

            } catch (Exception e) {
                // If Redis is down, log but don't block request
                log.error("Rate limit check failed: {}", e.getMessage());
                // Continue without rate limiting
            }
        }

        // Continue with filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract client IP address
     * Checks X-Forwarded-For header first (for proxied requests)
     * Falls back to remote address
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader != null && !xfHeader.isEmpty()) {
            // X-Forwarded-For can contain multiple IPs, take the first one
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}