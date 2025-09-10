package com.weather_found.weather_app.modules.weather.controller;

import com.weather_found.weather_app.modules.weather.service.WeatherCacheService;
import com.weather_found.weather_app.modules.weather.service.RedisMonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Enhanced REST controller for weather data management with Redis caching
 */
@RestController
@RequestMapping("/api/weather/cached")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Weather Management (Cached)", description = "Weather data endpoints with Redis caching")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class CachedWeatherController {

    private final WeatherCacheService weatherCacheService;
    private final RedisMonitoringService redisMonitoringService;

    /**
     * Get current weather for a location (with caching)
     */
    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get cached current weather", description = "Get current weather data for a specific location with Redis caching (TTL: 5 minutes)")
    public ResponseEntity<String> getCurrentWeatherCached(
            @Parameter(description = "Location name", required = true) @RequestParam String location,
            Authentication authentication) {

        // Track location popularity
        weatherCacheService.incrementLocationPopularity(location);

        String weatherData = weatherCacheService.getCurrentWeatherCached(location);
        return ResponseEntity.ok(weatherData);
    }

    /**
     * Get weather forecast (with caching)
     */
    @GetMapping("/forecast")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get cached weather forecast", description = "Get weather forecast for a specific location with Redis caching (TTL: 30 minutes)")
    public ResponseEntity<String> getWeatherForecastCached(
            @Parameter(description = "Location name", required = true) @RequestParam String location,
            @Parameter(description = "Number of forecast days", required = false) @RequestParam(defaultValue = "7") int days,
            Authentication authentication) {

        weatherCacheService.incrementLocationPopularity(location);

        String forecastData = weatherCacheService.getWeatherForecastCached(location, days);
        return ResponseEntity.ok(forecastData);
    }

    /**
     * Get historical weather data (with caching)
     */
    @GetMapping("/historical")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get cached historical weather", description = "Get historical weather data with Redis caching (TTL: 2 hours)")
    public ResponseEntity<String> getHistoricalWeatherCached(
            @Parameter(description = "Location name", required = true) @RequestParam String location,
            @Parameter(description = "Start date (YYYY-MM-DD)", required = true) @RequestParam String startDate,
            @Parameter(description = "End date (YYYY-MM-DD)", required = true) @RequestParam String endDate,
            Authentication authentication) {

        weatherCacheService.incrementLocationPopularity(location);

        String historicalData = weatherCacheService.getHistoricalWeatherCached(location, startDate, endDate);
        return ResponseEntity.ok(historicalData);
    }

    /**
     * Get popular locations
     */
    @GetMapping("/popular-locations")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get popular locations", description = "Get most frequently requested weather locations")
    public ResponseEntity<Map<String, Object>> getPopularLocations(
            @Parameter(description = "Number of locations to return") @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {

        Set<Object> popularLocations = weatherCacheService.getPopularLocations(limit);

        Map<String, Object> response = new HashMap<>();
        response.put("popularLocations", popularLocations);
        response.put("limit", limit);
        response.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Cache user weather preferences
     */
    @PostMapping("/preferences")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Cache user preferences", description = "Cache user weather preferences in Redis")
    public ResponseEntity<Map<String, Object>> cacheUserPreferences(
            @RequestBody Map<String, Object> preferences,
            Authentication authentication) {

        String userId = authentication.getName();
        weatherCacheService.cacheUserPreferences(userId, preferences);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "User preferences cached successfully");
        response.put("userId", userId);
        response.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Get user weather preferences
     */
    @GetMapping("/preferences")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user preferences", description = "Get cached user weather preferences from Redis")
    public ResponseEntity<Map<String, Object>> getUserPreferences(Authentication authentication) {
        String userId = authentication.getName();
        Object preferences = weatherCacheService.getUserPreferences(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("userId", userId);
        response.put("preferences", preferences);
        response.put("cached", preferences != null);
        response.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Cache weather alert
     */
    @PostMapping("/alerts/{location}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Cache weather alert", description = "Cache weather alert for a location (Admin only)")
    public ResponseEntity<Map<String, Object>> cacheWeatherAlert(
            @PathVariable String location,
            @RequestBody Map<String, Object> alert,
            Authentication authentication) {

        weatherCacheService.cacheWeatherAlert(location, alert);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Weather alert cached successfully");
        response.put("location", location);
        response.put("alert", alert);
        response.put("cachedBy", authentication.getName());
        response.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Get weather alert
     */
    @GetMapping("/alerts/{location}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get weather alert", description = "Get cached weather alert for a location")
    public ResponseEntity<Map<String, Object>> getWeatherAlert(
            @PathVariable String location,
            Authentication authentication) {

        Object alert = weatherCacheService.getWeatherAlert(location);

        Map<String, Object> response = new HashMap<>();
        response.put("location", location);
        response.put("alert", alert);
        response.put("hasAlert", alert != null);
        response.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Clear cache for a location
     */
    @DeleteMapping("/cache/{location}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Clear location cache", description = "Clear all cached data for a specific location (Admin only)")
    public ResponseEntity<Map<String, Object>> clearLocationCache(
            @PathVariable String location,
            Authentication authentication) {

        weatherCacheService.clearLocationCache(location);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Cache cleared successfully");
        response.put("location", location);
        response.put("clearedBy", authentication.getName());
        response.put("timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Get cache statistics
     */
    @GetMapping("/cache/stats")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get cache statistics", description = "Get Redis cache statistics and metrics (Admin only)")
    public ResponseEntity<String> getCacheStats(Authentication authentication) {
        Object stats = weatherCacheService.getCacheStats();
        return ResponseEntity.ok(stats.toString());
    }

    /**
     * Health check endpoint for Redis connectivity
     */
    @GetMapping("/health")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Cache health check", description = "Check Redis connectivity and cache health")
    public ResponseEntity<Map<String, Object>> healthCheck(Authentication authentication) {
        Map<String, Object> health = redisMonitoringService.checkHealth();
        health.put("checkedBy", authentication.getName());

        return ResponseEntity.ok(health);
    }
}
