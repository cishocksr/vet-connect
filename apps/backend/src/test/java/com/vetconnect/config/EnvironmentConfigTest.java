package com.vetconnect.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for EnvironmentConfig
 * Note: EnvironmentConfig is disabled in test profile by default,
 * so we test validation logic by temporarily enabling it
 */
@SpringBootTest(classes = EnvironmentConfig.class)
@TestPropertySource(properties = {
        "jwt.secret=ThisIsATestSecretKeyThatIsAtLeast64CharactersLongForHS512Algorithm",
        "spring.datasource.password=testpassword",
        "spring.data.redis.host=localhost",
        "cors.allowed-origins=http://localhost:3000",
        "spring.profiles.active=dev"
})
@ActiveProfiles("dev") // Override the !test profile restriction for testing
class EnvironmentConfigTest {

    @Autowired
    private EnvironmentConfig environmentConfig;

    @Test
    void environmentConfig_withValidConfiguration_shouldLoad() {
        assertNotNull(environmentConfig);
    }

    @Test
    void environmentConfig_validateEnvironment_shouldPassWithValidJwtSecret() {
        // The @PostConstruct method runs automatically
        // If it throws an exception, this test will fail
        assertDoesNotThrow(() -> environmentConfig.validateEnvironment());
    }
}
