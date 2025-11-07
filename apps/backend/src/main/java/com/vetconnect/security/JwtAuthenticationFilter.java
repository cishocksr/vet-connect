package com.vetconnect.security;

import com.vetconnect.service.TokenBlacklistService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

/**
 * JWT Authentication Filter
 *
 * Purpose: Intercepts every HTTP request to validate JWT tokens
 * Security: Checks token blacklist before authentication
 *
 * HOW IT WORKS:
 * 1. Extracts JWT token from Authorization header
 * 2. Validates token format and signature
 * 3. Checks if token is blacklisted (NEW!)
 * 4. Checks if all user's tokens are blacklisted (NEW!)
 * 5. If valid, sets authentication in SecurityContext
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // THESE ARE THE FIELDS - @RequiredArgsConstructor creates constructor for these
    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final TokenBlacklistService tokenBlacklistService;

    /**
     * This method runs for EVERY HTTP request
     * It checks if the request has a valid JWT token
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Step 1: Extract JWT from Authorization header
            String jwt = getJwtFromRequest(request);

            // Step 2: If we have a token, validate it
            if (jwt != null && tokenProvider.validateToken(jwt)) {

                // Step 3: NEW SECURITY CHECK - Is token blacklisted?
                if (tokenBlacklistService.isTokenBlacklisted(jwt)) {
                    log.warn("Attempted use of blacklisted token");
                    filterChain.doFilter(request, response);
                    return;
                }

                // Step 4: Extract user info from token
                String email = tokenProvider.getEmailFromToken(jwt);
                UUID userId = tokenProvider.getUserIdFromToken(jwt);
                Integer tokenVersion = tokenProvider.getTokenVersionFromToken(jwt);

                // Step 5: NEW SECURITY CHECK - Are all this user's tokens blacklisted?
                if (tokenBlacklistService.areAllUserTokensBlacklisted(userId.toString())) {
                    log.warn("Attempted use of token for user with all tokens blacklisted: {}", userId);
                    filterChain.doFilter(request, response);
                    return;
                }

                // Step 6: Load user details from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                // Step 7: NEW SECURITY CHECK - Validate token version
                // If user changed password or had security event, token version is incremented
                // Old tokens (with old version) become invalid instantly
                if (userDetails instanceof CustomUserDetails) {
                    CustomUserDetails customUserDetails = (CustomUserDetails) userDetails;
                    if (!tokenVersion.equals(customUserDetails.getTokenVersion())) {
                        log.warn("Token version mismatch for user: {}. Token version: {}, Current version: {}",
                                userId, tokenVersion, customUserDetails.getTokenVersion());
                        filterChain.doFilter(request, response);
                        return;
                    }
                }

                // Step 8: Create authentication object
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );

                // Step 9: Set additional details
                authentication.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // Step 10: Store authentication in SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("Set authentication for user: {}", email);
            }
        } catch (Exception e) {
            log.error("Cannot set user authentication: {}", e.getMessage());
        }

        // Step 11: Continue with the request
        filterChain.doFilter(request, response);
    }

    /**
     * Extract JWT token from Authorization header
     *
     * Authorization header format: "Bearer <token>"
     * This method extracts the <token> part
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            // Remove "Bearer " prefix (7 characters)
            return bearerToken.substring(7);
        }
        return null;
    }
}