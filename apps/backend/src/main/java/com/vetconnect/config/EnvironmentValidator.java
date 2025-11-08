package com.vetconnect.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates required environment variables on application startup
 * Fails fast if critical configuration is missing
 */
@Configuration
@Profile("prod")
@Slf4j
public class EnvironmentValidator {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${app.cors.allowed-origins:}")
    private String corsOrigins;

    @PostConstruct
    public void validateProductionConfig() {
        List<String> errors = new ArrayList<>();

        // Validate JWT secret
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            errors.add("JWT_SECRET environment variable is required");
        } else if (jwtSecret.length() < 64) {
            errors.add("JWT_SECRET must be at least 64 characters (use: openssl rand -base64 64)");
        }

        // Validate database password
        if (dbPassword == null || dbPassword.trim().isEmpty()) {
            errors.add("POSTGRES_PASSWORD environment variable is required");
        } else if (dbPassword.length() < 12) {
            log.warn("⚠️  Database password should be at least 12 characters");
        }

        // Validate CORS origins don't include localhost
        if (corsOrigins != null && (corsOrigins.contains("localhost") || corsOrigins.contains("127.0.0.1"))) {
            errors.add("CORS_ALLOWED_ORIGINS must not include localhost in production");
        }

        if (!errors.isEmpty()) {
            log.error("========================================");
            log.error("PRODUCTION CONFIGURATION ERRORS:");
            errors.forEach(error -> log.error("❌ {}", error));
            log.error("========================================");
            throw new IllegalStateException(
                    "Application cannot start in production mode with invalid configuration. " +
                            "See errors above."
            );
        }

        log.info("✅ All production environment variables validated successfully");
    }
}