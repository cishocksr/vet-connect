package com.vetconnect.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web Configuration
 *
 * PURPOSE: Configure CORS (Cross-Origin Resource Sharing)
 *
 * SECURITY NOTES:
 * - CORS prevents unauthorized websites from accessing your API
 * - In development: Allow localhost origins
 * - In production: Only allow your actual domain(s)
 * - Never use "*" (wildcard) in production!
 *
 * LEARNING NOTE:
 * CORS is a browser security feature. When a frontend (e.g., React on port 5173)
 * makes a request to a backend (e.g., Spring Boot on port 8080), the browser
 * checks if the backend allows that origin. Without CORS config, the browser
 * blocks the request even if the backend responds!
 */
@Configuration
@Slf4j
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    /**
     * Configure CORS mappings
     *
     * WHAT IT DOES:
     * - Specifies which domains can access your API
     * - Specifies which HTTP methods are allowed
     * - Specifies which headers are allowed
     * - Configures credential support (cookies, auth headers)
     *
     * SECURITY:
     * - Origins read from environment-specific config
     * - Development: localhost origins
     * - Production: Only your production domain(s)
     */
    @Override
    public void addCorsMappings(@NonNull CorsRegistry registry) {
        // Split comma-separated origins into array
        String[] origins = allowedOrigins.split(",");

        log.info("Configuring CORS with allowed origins: {}", allowedOrigins);

        registry.addMapping("/api/**")  // Apply to all /api/* endpoints
                .allowedOrigins(origins)  // Only these origins can access API
                .allowedMethods(
                        "GET",     // Read data
                        "POST",    // Create data
                        "PUT",     // Update data (full replacement)
                        "PATCH",   // Update data (partial)
                        "DELETE",  // Delete data
                        "OPTIONS"  // Preflight requests (browser checks)
                )
                .allowedHeaders("*")  // Allow all headers
                .allowCredentials(true)  // Allow cookies and Authorization header
                .maxAge(3600);  // Cache preflight response for 1 hour

        log.debug("CORS configuration completed");
    }
}