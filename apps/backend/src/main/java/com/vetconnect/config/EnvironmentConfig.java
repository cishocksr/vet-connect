package com.vetconnect.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

/**
 * Environment Configuration Validator
 *
 * Validates all critical environment variables at application startup
 * to fail fast if configuration is invalid
 *
 * NOTE: This config is skipped during tests (test profile)
 */
@Configuration
@Validated
@Slf4j
@Profile("!test")  // Don't load this config when running tests
public class EnvironmentConfig {

    @NotEmpty(message = "JWT_SECRET must be set")
    @Value("${jwt.secret}")
    private String jwtSecret;

    @NotEmpty(message = "POSTGRES_PASSWORD must be set")
    @Value("${spring.datasource.password}")
    private String dbPassword;

    @NotEmpty(message = "REDIS_HOST must be set")
    @Value("${spring.data.redis.host}")
    private String redisHost;

    @NotEmpty(message = "CORS origins must be configured")
    @Value("${cors.allowed-origins}")
    private String corsOrigins;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @PostConstruct
    public void validateEnvironment() {
        log.info("🔍 Validating environment configuration...");

        // Validate JWT secret length for HS512
        if (jwtSecret.length() < 64) {
            throw new IllegalStateException(
                    "❌ JWT_SECRET must be at least 64 characters for HS512 algorithm. " +
                            "Current length: " + jwtSecret.length() + ". " +
                            "Generate with: openssl rand -base64 64"
            );
        }

        // Validate production settings
        if ("prod".equals(activeProfile)) {
            if (corsOrigins.contains("localhost")) {
                throw new IllegalStateException(
                        "❌ CORS configuration includes localhost in production! " +
                                "Set CORS_ALLOWED_ORIGINS to your production domain only."
                );
            }

            if ("postgres".equals(dbPassword) || dbPassword.length() < 16) {
                log.warn("⚠️  WARNING: Using weak database password in production!");
            }
        }

        log.info("✅ Environment variables validated successfully");
        log.info("📋 Active Profile: {}", activeProfile);
    }
}