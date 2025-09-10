package com.weather_found.weather_app.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis configuration for weather data caching
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * Configure RedisTemplate for custom operations
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setHashValueSerializer(new GenericJackson2JsonRedisSerializer());

        template.afterPropertiesSet();
        return template;
    }

    /**
     * Configure cache manager with different TTL for different cache types
     */
    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration defaultCacheConfig = RedisCacheConfiguration.defaultCacheConfig()
                .serializeKeysWith(org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair
                                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .entryTtl(Duration.ofMinutes(15)); // Default 15 minutes

        // Configure different TTL for different cache types
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Current weather - 5 minutes (weather changes frequently)
        cacheConfigurations.put("currentWeather", defaultCacheConfig.entryTtl(Duration.ofMinutes(5)));

        // Weather forecast - 30 minutes (forecasts are more stable)
        cacheConfigurations.put("weatherForecast", defaultCacheConfig.entryTtl(Duration.ofMinutes(30)));

        // Historical weather - 2 hours (historical data doesn't change)
        cacheConfigurations.put("historicalWeather", defaultCacheConfig.entryTtl(Duration.ofHours(2)));

        // Weather alerts - 1 minute (critical information should be fresh)
        cacheConfigurations.put("weatherAlerts", defaultCacheConfig.entryTtl(Duration.ofMinutes(1)));

        // User preferences - 1 hour
        cacheConfigurations.put("userPreferences", defaultCacheConfig.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(defaultCacheConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }
}
