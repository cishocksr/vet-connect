package com.vetconnect.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for RedisConfig
 */
@SpringBootTest(classes = RedisConfig.class)
@TestPropertySource(properties = {
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "spring.data.redis.password=",
        "spring.data.redis.enabled=true"
})
class RedisConfigTest {

    @Autowired
    private RedisConfig redisConfig;

    @Test
    void redisConfig_shouldLoadSuccessfully() {
        assertNotNull(redisConfig);
    }

    @Test
    void redisConnectionFactory_shouldBeCreated() {
        RedisConnectionFactory factory = redisConfig.redisConnectionFactory();
        assertNotNull(factory);
    }

    @Test
    void redisTemplate_shouldBeConfigured() {
        RedisTemplate<String, String> template = redisConfig.redisTemplate();
        assertNotNull(template);
        assertNotNull(template.getKeySerializer());
        assertNotNull(template.getValueSerializer());
    }
}
