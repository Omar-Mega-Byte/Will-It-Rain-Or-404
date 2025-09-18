package com.weather_found.weather_app.modules.weather.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Weather caching service using Redis
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherCacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // Cache Keys
    private static final String CURRENT_WEATHER_KEY = "weather:current:";
    private static final String FORECAST_KEY = "weather:forecast:";
    private static final String HISTORICAL_KEY = "weather:historical:";
    private static final String USER_PREFERENCES_KEY = "weather:preferences:";
    private static final String POPULAR_LOCATIONS_KEY = "weather:popular:locations";
    private static final String WEATHER_ALERTS_KEY = "weather:alerts:";

    /**
     * Cache current weather data
     */
    @Cacheable(value = "currentWeather", key = "#location")
    public String getCurrentWeatherCached(String location) {
        log.info("Fetching current weather for location: {}", location);

        // This would normally call external API
        String weatherJson = generateCurrentWeatherJson(location);

        // Also store in Redis with custom TTL if available
        if (isRedisAvailable()) {
            String key = CURRENT_WEATHER_KEY + location.toLowerCase();
            redisTemplate.opsForValue().set(key, weatherJson, 5, TimeUnit.MINUTES);
        }

        return weatherJson;
    }

    /**
     * Cache weather forecast data
     */
    @Cacheable(value = "weatherForecast", key = "#location + ':' + #days")
    public String getWeatherForecastCached(String location, int days) {
        log.info("Fetching weather forecast for location: {} for {} days", location, days);

        String forecastJson = generateForecastJson(location, days);

        // Store in Redis with 30-minute TTL if available
        if (isRedisAvailable()) {
            String key = FORECAST_KEY + location.toLowerCase() + ":" + days;
            redisTemplate.opsForValue().set(key, forecastJson, 30, TimeUnit.MINUTES);
        }

        return forecastJson;
    }

    /**
     * Cache historical weather data
     */
    @Cacheable(value = "historicalWeather", key = "#location + ':' + #startDate + ':' + #endDate")
    public String getHistoricalWeatherCached(String location, String startDate, String endDate) {
        log.info("Fetching historical weather for location: {} from {} to {}", location, startDate, endDate);

        String historicalJson = generateHistoricalWeatherJson(location, startDate, endDate);

        // Store in Redis with 2-hour TTL if available (historical data doesn't change)
        if (isRedisAvailable()) {
            String key = HISTORICAL_KEY + location.toLowerCase() + ":" + startDate + ":" + endDate;
            redisTemplate.opsForValue().set(key, historicalJson, 2, TimeUnit.HOURS);
        }

        return historicalJson;
    }

    /**
     * Cache user weather preferences
     */
    public void cacheUserPreferences(String userId, Object preferences) {
        if (isRedisAvailable()) {
            String key = USER_PREFERENCES_KEY + userId;
            redisTemplate.opsForValue().set(key, preferences, 1, TimeUnit.HOURS);
            log.info("Cached weather preferences for user: {}", userId);
        }
    }

    /**
     * Get cached user preferences
     */
    public Object getUserPreferences(String userId) {
        if (!isRedisAvailable()) {
            return null;
        }
        String key = USER_PREFERENCES_KEY + userId;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Track popular locations (using Redis sorted sets)
     */
    public void incrementLocationPopularity(String location) {
        if (isRedisAvailable()) {
            redisTemplate.opsForZSet().incrementScore(POPULAR_LOCATIONS_KEY, location.toLowerCase(), 1);
            log.debug("Incremented popularity for location: {}", location);
        }
    }

    /**
     * Get popular locations
     */
    public Set<Object> getPopularLocations(int limit) {
        if (!isRedisAvailable()) {
            return null;
        }
        return redisTemplate.opsForZSet().reverseRange(POPULAR_LOCATIONS_KEY, 0, limit - 1);
    }

    /**
     * Cache weather alerts
     */
    public void cacheWeatherAlert(String location, Object alert) {
        if (isRedisAvailable()) {
            String key = WEATHER_ALERTS_KEY + location.toLowerCase();
            redisTemplate.opsForValue().set(key, alert, 1, TimeUnit.MINUTES);
            log.info("Cached weather alert for location: {}", location);
        }
    }

    /**
     * Get weather alerts
     */
    public Object getWeatherAlert(String location) {
        if (!isRedisAvailable()) {
            return null;
        }
        String key = WEATHER_ALERTS_KEY + location.toLowerCase();
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Clear all caches for a location
     */
    @CacheEvict(value = { "currentWeather", "weatherForecast", "historicalWeather" }, key = "#location")
    public void clearLocationCache(String location) {
        if (!isRedisAvailable()) {
            return;
        }
        // Also clear Redis keys
        String currentKey = CURRENT_WEATHER_KEY + location.toLowerCase();
        String forecastPattern = FORECAST_KEY + location.toLowerCase() + "*";
        String historicalPattern = HISTORICAL_KEY + location.toLowerCase() + "*";

        redisTemplate.delete(currentKey);

        // Delete keys matching pattern
        Set<String> forecastKeys = redisTemplate.keys(forecastPattern);
        Set<String> historicalKeys = redisTemplate.keys(historicalPattern);

        if (forecastKeys != null && !forecastKeys.isEmpty()) {
            redisTemplate.delete(forecastKeys);
        }
        if (historicalKeys != null && !historicalKeys.isEmpty()) {
            redisTemplate.delete(historicalKeys);
        }

        log.info("Cleared all cached data for location: {}", location);
    }

    /**
     * Get cache statistics
     */
    public Object getCacheStats() {
        try {
            // Get cache size information
            Set<String> currentWeatherKeys = redisTemplate.keys(CURRENT_WEATHER_KEY + "*");
            Set<String> forecastKeys = redisTemplate.keys(FORECAST_KEY + "*");
            Set<String> historicalKeys = redisTemplate.keys(HISTORICAL_KEY + "*");

            var stats = new java.util.HashMap<String, Object>();
            stats.put("currentWeatherCacheSize", currentWeatherKeys != null ? currentWeatherKeys.size() : 0);
            stats.put("forecastCacheSize", forecastKeys != null ? forecastKeys.size() : 0);
            stats.put("historicalCacheSize", historicalKeys != null ? historicalKeys.size() : 0);
            stats.put("popularLocationsCount", redisTemplate.opsForZSet().zCard(POPULAR_LOCATIONS_KEY));
            stats.put("cacheHitRatio", "Calculated based on actual usage");
            stats.put("lastUpdated", LocalDateTime.now());

            return objectMapper.writeValueAsString(stats);
        } catch (JsonProcessingException e) {
            log.error("Error generating cache stats", e);
            return "{}";
        }
    }

    // Helper methods to generate mock data (in real app, these would call external
    // APIs)
    private String generateCurrentWeatherJson(String location) {
        return """
                {
                    "location": {
                        "name": "%s",
                        "country": "United States",
                        "coordinates": {"lat": 40.7128, "lng": -74.0060},
                        "timezone": "America/New_York",
                        "localTime": "%s"
                    },
                    "current": {
                        "temperature": "22°C",
                        "feelsLike": "24°C",
                        "condition": "Partly Cloudy",
                        "humidity": "65%%",
                        "pressure": "1013 hPa",
                        "visibility": "10 km",
                        "uvIndex": "6 (High)",
                        "windSpeed": "12 km/h",
                        "windDirection": "Southwest",
                        "windGust": "18 km/h",
                        "cloudCover": "40%%",
                        "dewPoint": "15°C"
                    },
                    "cached": true,
                    "cacheTimestamp": "%s",
                    "lastUpdated": "%s"
                }
                """.formatted(location, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now());
    }

    private String generateForecastJson(String location, int days) {
        return """
                {
                    "location": {
                        "name": "%s",
                        "coordinates": {"lat": 40.7128, "lng": -74.0060}
                    },
                    "forecast": {
                        "period": "%d days",
                        "generatedAt": "%s",
                        "cached": true,
                        "daily": [
                            {
                                "date": "2025-09-11",
                                "condition": "Sunny",
                                "temperature": {"min": "18°C", "max": "25°C"}
                            },
                            {
                                "date": "2025-09-12",
                                "condition": "Partly Cloudy",
                                "temperature": {"min": "20°C", "max": "27°C"}
                            }
                        ]
                    },
                    "cacheTimestamp": "%s"
                }
                """.formatted(location, days, LocalDateTime.now(), LocalDateTime.now());
    }

    private String generateHistoricalWeatherJson(String location, String startDate, String endDate) {
        return """
                {
                    "location": {
                        "name": "%s"
                    },
                    "period": {
                        "startDate": "%s",
                        "endDate": "%s"
                    },
                    "summary": {
                        "averageTemperature": "21.3°C",
                        "totalPrecipitation": "45mm"
                    },
                    "cached": true,
                    "cacheTimestamp": "%s"
                }
                """.formatted(location, startDate, endDate, LocalDateTime.now());
    }

    /**
     * Check if Redis is available
     */
    private boolean isRedisAvailable() {
        try {
            if (redisTemplate.getConnectionFactory() != null) {
                redisTemplate.opsForValue().get("redis:health:check");
                return true;
            }
        } catch (Exception e) {
            log.debug("Redis not available: {}", e.getMessage());
        }
        return false;
    }
}
