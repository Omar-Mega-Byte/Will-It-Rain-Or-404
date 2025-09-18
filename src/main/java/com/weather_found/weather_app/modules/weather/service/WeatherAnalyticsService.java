package com.weather_found.weather_app.modules.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Redis-based analytics service for weather API usage
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherAnalyticsService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Analytics Keys
    private static final String DAILY_REQUESTS_KEY = "analytics:daily:requests:";
    private static final String HOURLY_REQUESTS_KEY = "analytics:hourly:requests:";
    private static final String USER_ACTIVITY_KEY = "analytics:user:activity:";
    private static final String ENDPOINT_USAGE_KEY = "analytics:endpoints:usage";
    private static final String LOCATION_REQUESTS_KEY = "analytics:locations:requests";
    private static final String ERROR_TRACKING_KEY = "analytics:errors:";

    /**
     * Track API request asynchronously
     */
    @Async
    public void trackApiRequest(String endpoint, String location, String userId) {
        if (!isRedisAvailable()) {
            log.debug("Redis not available, skipping analytics tracking");
            return;
        }
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String currentHour = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH"));

            // Increment daily requests
            String dailyKey = DAILY_REQUESTS_KEY + today;
            redisTemplate.opsForValue().increment(dailyKey);
            redisTemplate.expire(dailyKey, 30, TimeUnit.DAYS); // Keep for 30 days

            // Increment hourly requests
            String hourlyKey = HOURLY_REQUESTS_KEY + currentHour;
            redisTemplate.opsForValue().increment(hourlyKey);
            redisTemplate.expire(hourlyKey, 7, TimeUnit.DAYS); // Keep for 7 days

            // Track endpoint usage
            redisTemplate.opsForZSet().incrementScore(ENDPOINT_USAGE_KEY, endpoint, 1);

            // Track location requests
            if (location != null && !location.isEmpty()) {
                redisTemplate.opsForZSet().incrementScore(LOCATION_REQUESTS_KEY, location.toLowerCase(), 1);
            }

            // Track user activity
            if (userId != null && !userId.isEmpty()) {
                String userKey = USER_ACTIVITY_KEY + userId;
                redisTemplate.opsForZSet().incrementScore(userKey, endpoint, 1);
                redisTemplate.expire(userKey, 30, TimeUnit.DAYS);
            }

            log.debug("Tracked API request - Endpoint: {}, Location: {}, User: {}", endpoint, location, userId);
        } catch (Exception e) {
            log.error("Error tracking API request", e);
        }
    }

    /**
     * Track API errors
     */
    @Async
    public void trackError(String endpoint, String errorType, String userId) {
        if (!isRedisAvailable()) {
            log.debug("Redis not available, skipping error tracking");
            return;
        }
        try {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
            String errorKey = ERROR_TRACKING_KEY + today + ":" + errorType;

            redisTemplate.opsForValue().increment(errorKey);
            redisTemplate.expire(errorKey, 30, TimeUnit.DAYS);

            // Track per endpoint
            String endpointErrorKey = ERROR_TRACKING_KEY + today + ":endpoint:" + endpoint;
            redisTemplate.opsForValue().increment(endpointErrorKey);
            redisTemplate.expire(endpointErrorKey, 30, TimeUnit.DAYS);

            log.debug("Tracked error - Endpoint: {}, Type: {}, User: {}", endpoint, errorType, userId);
        } catch (Exception e) {
            log.error("Error tracking error", e);
        }
    }

    /**
     * Get daily request statistics
     */
    public Map<String, Object> getDailyStats(String date) {
        Map<String, Object> stats = new HashMap<>();

        if (!isRedisAvailable()) {
            stats.put("date", date);
            stats.put("totalRequests", 0);
            stats.put("generatedAt", LocalDateTime.now());
            stats.put("redisAvailable", false);
            return stats;
        }

        try {
            String dailyKey = DAILY_REQUESTS_KEY + date;
            Object requests = redisTemplate.opsForValue().get(dailyKey);

            stats.put("date", date);
            stats.put("totalRequests", requests != null ? requests : 0);
            stats.put("generatedAt", LocalDateTime.now());
            stats.put("redisAvailable", true);

        } catch (Exception e) {
            log.error("Error getting daily stats for date: {}", date, e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Get hourly request statistics for today
     */
    public Map<String, Object> getHourlyStatsToday() {
        Map<String, Object> stats = new HashMap<>();
        Map<String, Object> hourlyData = new HashMap<>();

        if (!isRedisAvailable()) {
            for (int hour = 0; hour < 24; hour++) {
                hourlyData.put(String.format("%02d:00", hour), 0);
            }
            stats.put("date", LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE));
            stats.put("hourlyRequests", hourlyData);
            stats.put("generatedAt", LocalDateTime.now());
            stats.put("redisAvailable", false);
            return stats;
        }

        try {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

            for (int hour = 0; hour < 24; hour++) {
                String hourKey = HOURLY_REQUESTS_KEY + today + "-" + String.format("%02d", hour);
                Object requests = redisTemplate.opsForValue().get(hourKey);
                hourlyData.put(String.format("%02d:00", hour), requests != null ? requests : 0);
            }

            stats.put("date", today);
            stats.put("hourlyData", hourlyData);
            stats.put("generatedAt", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error getting hourly stats", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Get top endpoints by usage
     */
    public Map<String, Object> getTopEndpoints(int limit) {
        Map<String, Object> stats = new HashMap<>();

        if (!isRedisAvailable()) {
            stats.put("topEndpoints", new HashMap<>());
            stats.put("limit", limit);
            stats.put("generatedAt", LocalDateTime.now());
            stats.put("redisAvailable", false);
            return stats;
        }

        try {
            Set<Object> topEndpoints = redisTemplate.opsForZSet().reverseRange(ENDPOINT_USAGE_KEY, 0, limit - 1);

            stats.put("topEndpoints", topEndpoints);
            stats.put("limit", limit);
            stats.put("generatedAt", LocalDateTime.now());
            stats.put("redisAvailable", true);

        } catch (Exception e) {
            log.error("Error getting top endpoints", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Get top requested locations
     */
    public Map<String, Object> getTopLocations(int limit) {
        Map<String, Object> stats = new HashMap<>();

        if (!isRedisAvailable()) {
            stats.put("topLocations", new HashMap<>());
            stats.put("limit", limit);
            stats.put("generatedAt", LocalDateTime.now());
            stats.put("redisAvailable", false);
            return stats;
        }

        try {
            Set<Object> topLocations = redisTemplate.opsForZSet().reverseRange(LOCATION_REQUESTS_KEY, 0, limit - 1);

            stats.put("topLocations", topLocations);
            stats.put("limit", limit);
            stats.put("generatedAt", LocalDateTime.now());
            stats.put("redisAvailable", true);

        } catch (Exception e) {
            log.error("Error getting top locations", e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Get user activity statistics
     */
    public Map<String, Object> getUserActivity(String userId, int limit) {
        Map<String, Object> stats = new HashMap<>();

        if (!isRedisAvailable()) {
            stats.put("userId", userId);
            stats.put("topEndpoints", new HashMap<>());
            stats.put("limit", limit);
            stats.put("generatedAt", LocalDateTime.now());
            stats.put("redisAvailable", false);
            return stats;
        }

        try {
            String userKey = USER_ACTIVITY_KEY + userId;
            Set<Object> userActivity = redisTemplate.opsForZSet().reverseRange(userKey, 0, limit - 1);

            stats.put("userId", userId);
            stats.put("topEndpoints", userActivity);
            stats.put("limit", limit);
            stats.put("generatedAt", LocalDateTime.now());
            stats.put("redisAvailable", true);

        } catch (Exception e) {
            log.error("Error getting user activity for user: {}", userId, e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Get error statistics for a specific date
     */
    public Map<String, Object> getErrorStats(String date) {
        Map<String, Object> stats = new HashMap<>();
        Map<String, Object> errorData = new HashMap<>();

        if (!isRedisAvailable()) {
            stats.put("date", date);
            stats.put("errors", errorData);
            stats.put("generatedAt", LocalDateTime.now());
            stats.put("redisAvailable", false);
            return stats;
        }

        try {
            String pattern = ERROR_TRACKING_KEY + date + "*";
            Set<String> errorKeys = redisTemplate.keys(pattern);

            if (errorKeys != null) {
                for (String key : errorKeys) {
                    Object count = redisTemplate.opsForValue().get(key);
                    String errorType = key.substring(key.lastIndexOf(":") + 1);
                    errorData.put(errorType, count != null ? count : 0);
                }
            }

            stats.put("date", date);
            stats.put("errors", errorData);
            stats.put("generatedAt", LocalDateTime.now());
            stats.put("redisAvailable", true);

        } catch (Exception e) {
            log.error("Error getting error stats for date: {}", date, e);
            stats.put("error", e.getMessage());
        }

        return stats;
    }

    /**
     * Get comprehensive analytics dashboard data
     */
    public Map<String, Object> getAnalyticsDashboard() {
        Map<String, Object> dashboard = new HashMap<>();

        try {
            String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);

            // Today's stats
            dashboard.put("todayStats", getDailyStats(today));

            // Hourly breakdown
            dashboard.put("hourlyStats", getHourlyStatsToday());

            // Top endpoints
            dashboard.put("topEndpoints", getTopEndpoints(10));

            // Top locations
            dashboard.put("topLocations", getTopLocations(10));

            // Error summary
            dashboard.put("errorStats", getErrorStats(today));

            dashboard.put("generatedAt", LocalDateTime.now());
            dashboard.put("dashboard", "weather-api-analytics");

        } catch (Exception e) {
            log.error("Error generating analytics dashboard", e);
            dashboard.put("error", e.getMessage());
        }

        return dashboard;
    }

    /**
     * Reset analytics data (for testing or maintenance)
     */
    public void resetAnalytics() {
        if (!isRedisAvailable()) {
            log.debug("Redis not available, skipping analytics reset");
            return;
        }
        try {
            Set<String> analyticsKeys = redisTemplate.keys("analytics:*");
            if (analyticsKeys != null && !analyticsKeys.isEmpty()) {
                redisTemplate.delete(analyticsKeys);
                log.info("Reset analytics data - {} keys deleted", analyticsKeys.size());
            }
        } catch (Exception e) {
            log.error("Error resetting analytics data", e);
        }
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
