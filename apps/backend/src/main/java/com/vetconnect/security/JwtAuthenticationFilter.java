package com.vetconnect.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT Authentication Filter
 *
 * This filter intercepts EVERY HTTP request and:
 * 1. Extracts JWT token from Authorization header
 * 2. Validates the token
 * 3. Loads user details
 * 4. Sets authentication in Spring Security context
 *
 * FILTER CHAIN ORDER:
 * Request → JwtAuthenticationFilter → Spring Security → Controller
 *
 * AUTHORIZATION HEADER FORMAT:
 * Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
 *
 * HOW IT WORKS:
 * - If token is valid → User is authenticated
 * - If token is invalid/missing → User is NOT authenticated
 * - Public endpoints (login, register) bypass this
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;

    /**
     * Main filter method - called for every request
     *
     * @param request HTTP request
     * @param response HTTP response
     * @param filterChain Filter chain to continue processing
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 1. Extract JWT token from request
            String jwt = extractJwtFromRequest(request);

            // 2. Validate token and authenticate user
            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {

                // 3. Get user ID from token
                UUID userId = tokenProvider.getUserIdFromToken(jwt);

                // 4. Load user details
                UserDetails userDetails = userDetailsService.loadUserById(userId);

                // 5. Create authentication token
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,  // No credentials needed (already authenticated by JWT)
                                userDetails.getAuthorities()
                        );

                // 6. Set additional details
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 7. Set authentication in security context
                // This tells Spring Security that this user is authenticated
                SecurityContextHolder.getContext().setAuthentication(authentication);

                log.debug("Successfully authenticated user with ID: {}", userId);
            }

        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
            // Don't throw exception - just continue without authentication
            // This allows public endpoints to work
        }

        // 8. Continue with the filter chain
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     *
     * Header format: "Bearer <token>"
     * This method extracts just the token part
     *
     * @param request HTTP request
     * @return JWT token string, or null if not present
     */
    private String extractJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Check if header exists and starts with "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            // Extract token (remove "Bearer " prefix)
            return bearerToken.substring(7);
        }

        return null;
    }
}