package com.vetconnect.config;

import jakarta.annotation.PostConstruct;
import jakarta.validation.constraints.NotEmpty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @NotEmpty(message = "CORS origins must be configured")
    @Value("${app.cors.allowed-origins}")  // Fixed: was cors.allowed-origins
    private String corsOrigins;

    @Value("${spring.profiles.active}")
    private String activeProfile;

    @Value("${app.upload.dir:uploads/profile-pictures}")  // Fixed: was file.upload-dir
    private String uploadDirectory;

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
            validateProductionSettings();
        }

        // Validate upload directory
        validateUploadDirectory();

        log.info("✅ Environment variables validated successfully");
        log.info("📋 Active Profile: {}", activeProfile);
    }

    /**
     * Validate production-specific settings
     */
    private void validateProductionSettings() {
        // Check CORS configuration
        if (corsOrigins.contains("localhost")) {
            throw new IllegalStateException(
                    "❌ CORS configuration includes localhost in production! " +
                            "Set CORS_ALLOWED_ORIGINS to your production domain only."
            );
        }

        // Check database password strength
        if ("postgres".equals(dbPassword) || dbPassword.length() < 16) {
            log.warn("⚠️  WARNING: Using weak database password in production!");
        }

        // Check Redis password
        if (redisPassword == null || redisPassword.isEmpty()) {
            log.warn("⚠️  WARNING: Redis has no password configured in production!");
            log.warn("⚠️  Set REDIS_PASSWORD environment variable for production security");
        }

        log.info("🔐 Production security settings validated");
    }

    /**
     * Validate upload directory exists and is writable
     */
    private void validateUploadDirectory() {
        Path uploadPath = Paths.get(uploadDirectory);

        // Create directory if it doesn't exist
        if (!Files.exists(uploadPath)) {
            try {
                Files.createDirectories(uploadPath);
                log.info("✅ Created upload directory: {}", uploadPath.toAbsolutePath());
            } catch (IOException e) {
                throw new IllegalStateException(
                        "❌ Cannot create upload directory: " + uploadPath.toAbsolutePath(), e
                );
            }
        }

        // Verify directory is writable
        if (!Files.isWritable(uploadPath)) {
            throw new IllegalStateException(
                    "❌ Upload directory is not writable: " + uploadPath.toAbsolutePath()
            );
        }

        log.info("📁 Upload directory validated: {}", uploadPath.toAbsolutePath());
    }
}