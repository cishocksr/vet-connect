package com.vetconnect.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Request ID Filter
 *
 * PURPOSE: Add unique request ID to every request for tracing
 *
 * BENEFITS:
 * - Track single request across multiple log entries
 * - Correlate frontend and backend logs
 * - Debug production issues more easily
 *
 * IMPLEMENTATION:
 * 1. Generate unique request ID (or use existing from header)
 * 2. Add to MDC (Mapped Diagnostic Context) for logging
 * 3. Add to response header for client-side correlation
 *
 * USAGE IN LOGS:
 * Update logging pattern to include %X{requestId}
 * Example: "%d{yyyy-MM-dd HH:mm:ss} [%X{requestId}] - %msg%n"
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter implements Filter {

    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String REQUEST_ID_MDC_KEY = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Check if client sent request ID (for correlation)
            String requestId = httpRequest.getHeader(REQUEST_ID_HEADER);

            // If not, generate new one
            if (requestId == null || requestId.isEmpty()) {
                requestId = generateRequestId();
            }

            // Add to MDC for logging
            MDC.put(REQUEST_ID_MDC_KEY, requestId);

            // Add to response header so client can correlate
            httpResponse.setHeader(REQUEST_ID_HEADER, requestId);

            // Continue with request
            chain.doFilter(request, response);

        } finally {
            // Always clean up MDC to prevent memory leaks
            MDC.clear();
        }
    }

    /**
     * Generate unique request ID
     * Uses UUID for guaranteed uniqueness
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}

