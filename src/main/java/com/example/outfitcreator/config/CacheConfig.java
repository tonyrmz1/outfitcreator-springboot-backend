package com.example.outfitcreator.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Cache configuration for frequently accessed data.
 * Uses Caffeine as the caching provider for high-performance in-memory caching.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configure Caffeine cache manager with specific cache settings.
     * 
     * Cache specifications:
     * - users: User profiles cache (max 500 entries, 10 minutes TTL)
     * - colorWheel: Color wheel data cache (max 100 entries, 1 hour TTL)
     */
    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager("users", "colorWheel");
        cacheManager.setCaffeine(caffeineCacheBuilder());
        return cacheManager;
    }

    /**
     * Default Caffeine cache builder configuration.
     */
    private Caffeine<Object, Object> caffeineCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(500)
                .expireAfterAccess(10, TimeUnit.MINUTES)
                .recordStats();
    }

    /**
     * Specific cache builder for color wheel data (longer TTL).
     */
    @Bean
    public Caffeine<Object, Object> colorWheelCacheBuilder() {
        return Caffeine.newBuilder()
                .maximumSize(100)
                .expireAfterAccess(1, TimeUnit.HOURS)
                .recordStats();
    }
}
