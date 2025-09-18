package com.weather_found.weather_app.modules.weather.service;

import com.weather_found.weather_app.modules.weather.model.Location;
import com.weather_found.weather_app.modules.weather.model.WeatherAlert;
import com.weather_found.weather_app.modules.weather.model.WeatherDataEntity;
import com.weather_found.weather_app.modules.weather.repository.WeatherLocationRepository;
import com.weather_found.weather_app.modules.weather.repository.WeatherAlertRepository;
import com.weather_found.weather_app.modules.weather.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Service for providing weather dashboard data and analytics
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherDashboardService {

    private final WeatherDataRepository weatherDataRepository;
    private final WeatherLocationRepository locationRepository;
    private final WeatherAlertRepository alertRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final WeatherLocationService locationService;
    private final WeatherAlertService alertService;
    private final ExternalWeatherApiService externalWeatherApiService;

    // Cache keys
    private static final String DASHBOARD_CACHE_KEY = "dashboard:weather:";
    private static final String ANALYTICS_CACHE_KEY = "analytics:weather:";
    private static final String SUMMARY_CACHE_KEY = "summary:weather:";

    /**
     * Get comprehensive weather dashboard data
     */
    public Map<String, Object> getWeatherDashboard(Long userId, String userRole) {
        String cacheKey = DASHBOARD_CACHE_KEY + userId + ":" + userRole;

        if (isRedisConnected()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cachedDashboard = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedDashboard != null) {
                return cachedDashboard;
            }
        }

        Map<String, Object> dashboard = new HashMap<>();

        try {
            // Current weather summary
            dashboard.put("currentWeatherSummary", getCurrentWeatherSummary());

            // Active alerts
            dashboard.put("activeAlerts", getActiveAlertsSummary());

            // Popular locations
            dashboard.put("popularLocations", getPopularLocationsSummary());

            // Weather analytics (admin only)
            if ("ADMIN".equals(userRole)) {
                dashboard.put("analytics", getWeatherAnalytics());
                dashboard.put("systemMetrics", getSystemMetrics());
            }

            // Recent weather data
            dashboard.put("recentWeatherData", getRecentWeatherData());

            // Weather trends
            dashboard.put("weatherTrends", getWeatherTrends());

            dashboard.put("lastUpdated", LocalDateTime.now());

            // Cache for 10 minutes if Redis is available
            if (isRedisConnected()) {
                redisTemplate.opsForValue().set(cacheKey, dashboard, 10, TimeUnit.MINUTES);
            }

        } catch (Exception e) {
            log.error("Error building weather dashboard", e);
            dashboard.put("error", "Failed to load dashboard data");
        }

        return dashboard;
    }

    /**
     * Get weather summary for a specific location
     */
    public Map<String, Object> getLocationWeatherSummary(Long locationId) {
        String cacheKey = SUMMARY_CACHE_KEY + "location:" + locationId;

        if (isRedisConnected()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cachedSummary = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedSummary != null) {
                return cachedSummary;
            }
        }

        Map<String, Object> summary = new HashMap<>();

        try {
            Optional<Location> locationOpt = locationRepository.findById(locationId);
            if (locationOpt.isEmpty()) {
                summary.put("error", "Location not found");
                return summary;
            }

            Location location = locationOpt.get();

            // Current weather
            Map<String, Object> currentWeather = externalWeatherApiService.aggregateWeatherData(location.getName());
            summary.put("currentWeather", currentWeather);

            // Active alerts for this location
            List<WeatherAlert> locationAlerts = alertService.getActiveAlertsForLocation(locationId);
            summary.put("activeAlerts", locationAlerts);

            // Recent weather history
            List<WeatherDataEntity> recentData = weatherDataRepository
                    .findByLocationOrderByTimestampDesc(location).stream()
                    .limit(24) // Last 24 readings
                    .collect(Collectors.toList());
            summary.put("recentHistory", recentData);

            // Location statistics
            summary.put("locationStats", getLocationStatistics(location));

            summary.put("location", location);
            summary.put("lastUpdated", LocalDateTime.now());

            // Cache for 5 minutes if Redis is available
            if (isRedisConnected()) {
                redisTemplate.opsForValue().set(cacheKey, summary, 5, TimeUnit.MINUTES);
            }

        } catch (Exception e) {
            log.error("Error building location weather summary for location: {}", locationId, e);
            summary.put("error", "Failed to load location summary");
        }

        return summary;
    }

    /**
     * Get weather analytics data
     */
    public Map<String, Object> getWeatherAnalytics() {
        String cacheKey = ANALYTICS_CACHE_KEY + "general";

        if (isRedisConnected()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cachedAnalytics = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedAnalytics != null) {
                return cachedAnalytics;
            }
        }

        Map<String, Object> analytics = new HashMap<>();

        try {
            // Total locations tracked
            long totalLocations = locationRepository.count();
            analytics.put("totalLocations", totalLocations);

            // Total weather data points
            long totalWeatherData = weatherDataRepository.count();
            analytics.put("totalWeatherDataPoints", totalWeatherData);

            // Active alerts breakdown
            Map<String, Long> alertBreakdown = new HashMap<>();
            alertBreakdown.put("critical", alertRepository.countBySeverityAndStatus("CRITICAL", "ACTIVE"));
            alertBreakdown.put("high", alertRepository.countBySeverityAndStatus("HIGH", "ACTIVE"));
            alertBreakdown.put("medium", alertRepository.countBySeverityAndStatus("MEDIUM", "ACTIVE"));
            alertBreakdown.put("low", alertRepository.countBySeverityAndStatus("LOW", "ACTIVE"));
            analytics.put("alertBreakdown", alertBreakdown);

            // Data collection trends (last 7 days)
            analytics.put("dataCollectionTrends", getDataCollectionTrends());

            // Popular weather conditions
            analytics.put("popularConditions", getPopularWeatherConditions());

            // Temperature statistics
            analytics.put("temperatureStats", getTemperatureStatistics());

            // Cache for 15 minutes if Redis is available
            if (isRedisConnected()) {
                redisTemplate.opsForValue().set(cacheKey, analytics, 15, TimeUnit.MINUTES);
            }

        } catch (Exception e) {
            log.error("Error building weather analytics", e);
            analytics.put("error", "Failed to load analytics data");
        }

        return analytics;
    }

    /**
     * Get real-time weather metrics for monitoring
     */
    public Map<String, Object> getSystemMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        try {
            // Redis connection status
            metrics.put("redisConnected", isRedisConnected());

            // Database metrics
            metrics.put("activeConnections", "N/A"); // Would need database pool metrics

            // Cache hit rates
            metrics.put("cacheHitRates", getCacheHitRates());

            // API call statistics
            metrics.put("apiCallStats", getApiCallStatistics());

            // System health
            metrics.put("systemHealth", "HEALTHY");
            metrics.put("lastHealthCheck", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error getting system metrics", e);
            metrics.put("systemHealth", "ERROR");
            metrics.put("error", e.getMessage());
        }

        return metrics;
    }

    /**
     * Get weather forecast dashboard data
     */
    public Map<String, Object> getForecastDashboard(Long locationId, int days) {
        String cacheKey = DASHBOARD_CACHE_KEY + "forecast:" + locationId + ":" + days;

        if (isRedisConnected()) {
            @SuppressWarnings("unchecked")
            Map<String, Object> cachedForecast = (Map<String, Object>) redisTemplate.opsForValue().get(cacheKey);

            if (cachedForecast != null) {
                return cachedForecast;
            }
        }

        Map<String, Object> forecast = new HashMap<>();

        try {
            Optional<Location> locationOpt = locationRepository.findById(locationId);
            if (locationOpt.isEmpty()) {
                forecast.put("error", "Location not found");
                return forecast;
            }

            Location location = locationOpt.get();

            // Get forecast data from external API
            Map<String, Object> forecastData = externalWeatherApiService.getWeatherForecast(
                    location.getLatitude(), location.getLongitude(), days);

            forecast.put("location", location);
            forecast.put("forecastData", forecastData);
            forecast.put("days", days);
            forecast.put("generatedAt", LocalDateTime.now());

            // Cache for 30 minutes if Redis is available
            if (isRedisConnected()) {
                redisTemplate.opsForValue().set(cacheKey, forecast, 30, TimeUnit.MINUTES);
            }

        } catch (Exception e) {
            log.error("Error building forecast dashboard for location: {}", locationId, e);
            forecast.put("error", "Failed to load forecast data");
        }

        return forecast;
    }

    // Helper methods
    private Map<String, Object> getCurrentWeatherSummary() {
        Map<String, Object> summary = new HashMap<>();

        try {
            List<Location> topLocations = locationRepository.findAll().stream()
                    .limit(5)
                    .collect(Collectors.toList());

            List<Map<String, Object>> currentConditions = new ArrayList<>();

            for (Location location : topLocations) {
                try {
                    Map<String, Object> weather = externalWeatherApiService.getCurrentWeather(
                            location.getLatitude(), location.getLongitude());
                    weather.put("locationName", location.getName());
                    currentConditions.add(weather);
                } catch (Exception e) {
                    log.debug("Error getting current weather for location: {}", location.getName());
                }
            }

            summary.put("topLocations", currentConditions);
            summary.put("totalLocationsTracked", locationRepository.count());

        } catch (Exception e) {
            log.error("Error building current weather summary", e);
        }

        return summary;
    }

    private Map<String, Object> getActiveAlertsSummary() {
        Map<String, Object> summary = new HashMap<>();

        try {
            List<WeatherAlert> criticalAlerts = alertService.getCriticalAlerts();
            summary.put("criticalAlerts", criticalAlerts);
            summary.put("criticalCount", criticalAlerts.size());

            Map<String, Object> alertStats = alertService.getAlertStatistics();
            summary.put("totalActive", alertStats.get("totalActiveAlerts"));
            summary.put("breakdown", Map.of(
                    "critical", alertStats.get("criticalAlerts"),
                    "high", alertStats.get("highAlerts"),
                    "medium", alertStats.get("mediumAlerts"),
                    "low", alertStats.get("lowAlerts")));

        } catch (Exception e) {
            log.error("Error building active alerts summary", e);
        }

        return summary;
    }

    private List<Map<String, Object>> getPopularLocationsSummary() {
        // Redis-based popular locations removed; return empty or static list
        return new ArrayList<>();
    }

    private List<Map<String, Object>> getRecentWeatherData() {
        try {
            LocalDateTime since = LocalDateTime.now().minusHours(6);
            return weatherDataRepository.findRecentWeatherData(since).stream()
                    .limit(20)
                    .map(data -> {
                        Map<String, Object> dataInfo = new HashMap<>();
                        dataInfo.put("locationName", data.getLocation().getName());
                        dataInfo.put("temperature", data.getTemperature());
                        dataInfo.put("humidity", data.getHumidity());
                        dataInfo.put("weatherCondition", data.getWeatherCondition());
                        dataInfo.put("timestamp", data.getRecordedAt());
                        return dataInfo;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting recent weather data", e);
            return Collections.emptyList();
        }
    }

    private Map<String, Object> getWeatherTrends() {
        Map<String, Object> trends = new HashMap<>();

        try {
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);

            // Temperature trends
            List<Object[]> tempTrends = weatherDataRepository.getTemperatureTrends(weekAgo);
            trends.put("temperatureTrends", tempTrends);

            // Humidity trends
            List<Object[]> humidityTrends = weatherDataRepository.getHumidityTrends(weekAgo);
            trends.put("humidityTrends", humidityTrends);

        } catch (Exception e) {
            log.error("Error getting weather trends", e);
        }

        return trends;
    }

    private Map<String, Object> getLocationStatistics(Location location) {
        Map<String, Object> stats = new HashMap<>();

        try {
            LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);

            // Average temperature for the month
            Double avgTemp = weatherDataRepository.getAverageTemperatureByLocation(location, monthAgo);
            stats.put("averageTemperature", avgTemp);

            // Max and min temperatures
            Double maxTemp = weatherDataRepository.getMaxTemperatureByLocation(location, monthAgo);
            Double minTemp = weatherDataRepository.getMinTemperatureByLocation(location, monthAgo);
            stats.put("maxTemperature", maxTemp);
            stats.put("minTemperature", minTemp);

            // Alert count for this location
            long alertCount = alertRepository.countByLocationAndStatus(location, "ACTIVE");
            stats.put("activeAlerts", alertCount);

            // Data points collected
            long dataPoints = weatherDataRepository.countByLocationAndTimestampAfter(location, monthAgo);
            stats.put("dataPointsCollected", dataPoints);

        } catch (Exception e) {
            log.error("Error getting location statistics for: {}", location.getName(), e);
        }

        return stats;
    }

    private Map<String, Object> getDataCollectionTrends() {
        Map<String, Object> trends = new HashMap<>();

        try {
            LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
            List<Object[]> dailyCounts = weatherDataRepository.getDailyDataCounts(weekAgo);
            trends.put("dailyDataCounts", dailyCounts);

        } catch (Exception e) {
            log.error("Error getting data collection trends", e);
        }

        return trends;
    }

    private List<Map<String, Object>> getPopularWeatherConditions() {
        try {
            LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
            List<Object[]> rawData = weatherDataRepository.getPopularWeatherConditionsRaw(monthAgo);

            return rawData.stream()
                    .map(row -> {
                        Map<String, Object> condition = new HashMap<>();
                        condition.put("condition", row[0]);
                        condition.put("count", row[1]);
                        return condition;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error getting popular weather conditions", e);
            return Collections.emptyList();
        }
    }

    private Map<String, Object> getTemperatureStatistics() {
        Map<String, Object> stats = new HashMap<>();

        try {
            LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);

            Double avgTemp = weatherDataRepository.getGlobalAverageTemperature(monthAgo);
            Double maxTemp = weatherDataRepository.getGlobalMaxTemperature(monthAgo);
            Double minTemp = weatherDataRepository.getGlobalMinTemperature(monthAgo);

            stats.put("globalAverage", avgTemp);
            stats.put("globalMax", maxTemp);
            stats.put("globalMin", minTemp);

        } catch (Exception e) {
            log.error("Error getting temperature statistics", e);
        }

        return stats;
    }

    private boolean isRedisConnected() {
        try {
            if (redisTemplate.getConnectionFactory() != null) {
                redisTemplate.opsForValue().get("redis:health:check");
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> getCacheHitRates() {
        // This would require custom Redis metrics tracking
        // For now, return placeholder data
        Map<String, Object> rates = new HashMap<>();
        rates.put("overall", "N/A");
        rates.put("weather", "N/A");
        rates.put("locations", "N/A");
        return rates;
    }

    private Map<String, Object> getApiCallStatistics() {
        // This would require tracking API calls in Redis
        // For now, return placeholder data
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalToday", "N/A");
        stats.put("averageResponseTime", "N/A");
        stats.put("errorRate", "N/A");
        return stats;
    }
}
