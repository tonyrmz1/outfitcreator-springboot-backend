package com.example.outfitcreator.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Test to verify configuration properties are loaded correctly.
 */
@SpringBootTest
@ActiveProfiles("test")
class ConfigurationPropertiesTest {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    @Value("${storage.base-path}")
    private String storageBasePath;

    @Value("${storage.max-file-size}")
    private long storageMaxFileSize;

    @Value("${storage.max-resolution}")
    private int storageMaxResolution;

    @Value("${spring.data.web.pageable.default-page-size}")
    private int defaultPageSize;

    @Value("${spring.data.web.pageable.max-page-size}")
    private int maxPageSize;

    @Value("${cors.allowed-origins}")
    private String corsAllowedOrigins;

    @Value("${cors.allowed-methods}")
    private String corsAllowedMethods;

    @Value("${cors.allowed-headers}")
    private String corsAllowedHeaders;

    @Value("${cors.allow-credentials}")
    private boolean corsAllowCredentials;

    @Value("${cors.max-age}")
    private long corsMaxAge;

    @Test
    void shouldLoadJwtConfiguration() {
        assertThat(jwtSecret).isNotNull();
        assertThat(jwtSecret).isNotEmpty();
        assertThat(jwtExpiration).isGreaterThan(0);
    }

    @Test
    void shouldLoadStorageConfiguration() {
        assertThat(storageBasePath).isNotNull();
        assertThat(storageBasePath).isEqualTo("target/test-storage");
        assertThat(storageMaxFileSize).isEqualTo(5242880); // 5MB
        assertThat(storageMaxResolution).isEqualTo(1920);
    }

    @Test
    void shouldLoadPaginationConfiguration() {
        assertThat(defaultPageSize).isEqualTo(20);
        assertThat(maxPageSize).isEqualTo(100);
    }

    @Test
    void shouldLoadCorsConfiguration() {
        assertThat(corsAllowedOrigins).isNotNull();
        assertThat(corsAllowedMethods).isNotNull();
        assertThat(corsAllowedHeaders).isNotNull();
        assertThat(corsAllowCredentials).isTrue();
        assertThat(corsMaxAge).isEqualTo(3600);
    }

    @Test
    void shouldHaveValidJwtSecretLength() {
        // JWT secret must be at least 256 bits (32 characters) for HS256
        assertThat(jwtSecret.length()).isGreaterThanOrEqualTo(32);
    }

    @Test
    void shouldHaveReasonableJwtExpiration() {
        // JWT expiration should be between 1 hour and 7 days
        long oneHour = 3600000;
        long sevenDays = 604800000;
        assertThat(jwtExpiration).isBetween(oneHour, sevenDays);
    }

    @Test
    void shouldHaveValidStorageFileSize() {
        // Max file size should be 5MB as per requirements
        assertThat(storageMaxFileSize).isEqualTo(5 * 1024 * 1024);
    }

    @Test
    void shouldHaveValidPaginationDefaults() {
        // Default page size should be 20 as per requirements
        assertThat(defaultPageSize).isEqualTo(20);
        // Max page size should be reasonable
        assertThat(maxPageSize).isGreaterThan(defaultPageSize);
    }
}
