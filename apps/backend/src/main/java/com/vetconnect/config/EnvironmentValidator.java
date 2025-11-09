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
 *
 * IMPORTANT: Runs on ALL profiles EXCEPT dev
 * This ensures production validation happens even if someone
 * forgets to set SPRING_PROFILES_ACTIVE=prod
 */
@Configuration
@Profile("!dev")  // CHANGED: Run on all profiles except dev (was just "prod")
@Slf4j
public class EnvironmentValidator {

    @Value("${jwt.secret:}")
    private String jwtSecret;

    @Value("${spring.datasource.password:}")
    private String dbPassword;

    @Value("${app.cors.allowed-origins:}")
    private String corsOrigins;

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @PostConstruct
    public void validateProductionConfig() {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        log.info("========================================");
        log.info("VALIDATING ENVIRONMENT CONFIGURATION");
        log.info("Active Profile: {}", activeProfile);
        log.info("========================================");

        // Validate JWT secret
        if (jwtSecret == null || jwtSecret.trim().isEmpty()) {
            errors.add("JWT_SECRET environment variable is required");
        } else if (jwtSecret.length() < 64) {
            errors.add("JWT_SECRET must be at least 64 characters (use: openssl rand -base64 64)");
        } else if (jwtSecret.contains("CHANGE_THIS") || jwtSecret.contains("REPLACE_THIS")) {
            errors.add("JWT_SECRET contains placeholder text - must be replaced with actual secret");
        }

        // Validate database password
        if (dbPassword == null || dbPassword.trim().isEmpty()) {
            errors.add("POSTGRES_PASSWORD environment variable is required");
        } else if (dbPassword.contains("CHANGE_THIS") || dbPassword.contains("password")) {
            errors.add("POSTGRES_PASSWORD contains weak/placeholder value - use strong password");
        } else if (dbPassword.length() < 12) {
            warnings.add("Database password should be at least 12 characters (current: " + dbPassword.length() + ")");
        }

        // Validate CORS origins
        if (corsOrigins == null || corsOrigins.trim().isEmpty()) {
            warnings.add("CORS_ALLOWED_ORIGINS is not set - API will not be accessible from frontend");
        } else if ("prod".equalsIgnoreCase(activeProfile)) {
            // Strict validation for production
            if (corsOrigins.contains("localhost") || corsOrigins.contains("127.0.0.1")) {
                errors.add("CORS_ALLOWED_ORIGINS must not include localhost in production");
            }
            if (corsOrigins.contains("*")) {
                errors.add("CORS_ALLOWED_ORIGINS must not use wildcard (*) in production");
            }
        }

        // Display results
        if (!warnings.isEmpty()) {
            log.warn("========================================");
            log.warn("CONFIGURATION WARNINGS:");
            warnings.forEach(warning -> log.warn("⚠️  {}", warning));
            log.warn("========================================");
        }

        if (!errors.isEmpty()) {
            log.error("========================================");
            log.error("CRITICAL CONFIGURATION ERRORS:");
            errors.forEach(error -> log.error("❌ {}", error));
            log.error("========================================");
            log.error("");
            log.error("Application CANNOT start with invalid configuration.");
            log.error("Please set the required environment variables and try again.");
            log.error("");
            throw new IllegalStateException(
                    "Application cannot start with invalid configuration. " +
                            errors.size() + " error(s) found. See logs above."
            );
        }

        log.info("✅ All environment variables validated successfully");
        log.info("========================================");
    }
}