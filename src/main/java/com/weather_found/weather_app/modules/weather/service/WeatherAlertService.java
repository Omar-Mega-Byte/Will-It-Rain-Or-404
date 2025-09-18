package com.weather_found.weather_app.modules.weather.service;

import com.weather_found.weather_app.modules.weather.model.Location;
import com.weather_found.weather_app.modules.weather.model.WeatherAlert;
import com.weather_found.weather_app.modules.weather.repository.WeatherLocationRepository;
import com.weather_found.weather_app.modules.weather.repository.WeatherAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Service for managing weather alerts and notifications
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherAlertService {

    private final WeatherAlertRepository alertRepository;
    private final WeatherLocationRepository locationRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ExternalWeatherApiService externalWeatherApiService;

    // Cache keys
    private static final String ALERT_CACHE_KEY = "alerts:location:";
    private static final String ALERT_SUBSCRIPTION_KEY = "alerts:subscriptions:";
    private static final String ALERT_HISTORY_KEY = "alerts:history:";

    /**
     * Create a new weather alert
     */
    public WeatherAlert createAlert(Long locationId, String alertType, String title,
            String description, String severity, LocalDateTime alertTime,
            LocalDateTime expiresAt, String dataSource) {
        Optional<Location> locationOpt = locationRepository.findById(locationId);
        if (locationOpt.isEmpty()) {
            throw new IllegalArgumentException("Location not found: " + locationId);
        }

        Location location = locationOpt.get();

        WeatherAlert alert = new WeatherAlert(location, alertType, title, description,
                severity, alertTime, expiresAt, dataSource);

        WeatherAlert savedAlert = alertRepository.save(alert);

        // Cache the alert
        cacheLocationAlerts(location);

        // Trigger notifications
        processAlertNotifications(savedAlert);

        log.info("Created weather alert: {} for location: {}", savedAlert.getId(), location.getName());
        return savedAlert;
    }

    /**
     * Get active alerts for a location
     */
    public List<WeatherAlert> getActiveAlertsForLocation(Long locationId) {
        Optional<Location> locationOpt = locationRepository.findById(locationId);
        if (locationOpt.isEmpty()) {
            return Collections.emptyList();
        }

        Location location = locationOpt.get();
        String cacheKey = ALERT_CACHE_KEY + locationId + ":active";

        @SuppressWarnings("unchecked")
        List<WeatherAlert> cachedAlerts = (List<WeatherAlert>) redisTemplate.opsForValue().get(cacheKey);

        if (cachedAlerts != null) {
            return cachedAlerts;
        }

        List<WeatherAlert> alerts = alertRepository.findByLocationAndStatusOrderByAlertTimeDesc(location, "ACTIVE");

        // Cache for 5 minutes
        redisTemplate.opsForValue().set(cacheKey, alerts, 5, TimeUnit.MINUTES);

        return alerts;
    }

    /**
     * Get all active alerts
     */
    public List<WeatherAlert> getAllActiveAlerts() {
        return alertRepository.findByStatusOrderByAlertTimeDesc("ACTIVE");
    }

    /**
     * Get alerts by severity level
     */
    public List<WeatherAlert> getAlertsBySeverity(String severity) {
        return alertRepository.findBySeverityAndStatusOrderByAlertTimeDesc(severity, "ACTIVE");
    }

    /**
     * Get critical alerts
     */
    public List<WeatherAlert> getCriticalAlerts() {
        return alertRepository.findCriticalAlerts("ACTIVE");
    }

    /**
     * Subscribe user to alerts for a location
     */
    public void subscribeUserToLocationAlerts(Long userId, Long locationId, Set<String> alertTypes) {
        String key = ALERT_SUBSCRIPTION_KEY + userId;

        Map<String, Object> subscription = new HashMap<>();
        subscription.put("locationId", locationId);
        subscription.put("alertTypes", alertTypes);
        subscription.put("subscribedAt", LocalDateTime.now());

        redisTemplate.opsForHash().put(key, locationId.toString(), subscription);
        redisTemplate.expire(key, 30, TimeUnit.DAYS);

        log.info("User {} subscribed to alerts for location: {}", userId, locationId);
    }

    /**
     * Unsubscribe user from location alerts
     */
    public void unsubscribeUserFromLocationAlerts(Long userId, Long locationId) {
        String key = ALERT_SUBSCRIPTION_KEY + userId;
        redisTemplate.opsForHash().delete(key, locationId.toString());

        log.info("User {} unsubscribed from alerts for location: {}", userId, locationId);
    }

    /**
     * Get user alert subscriptions
     */
    public Map<Object, Object> getUserAlertSubscriptions(Long userId) {
        String key = ALERT_SUBSCRIPTION_KEY + userId;
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * Update alert status
     */
    public void updateAlertStatus(Long alertId, String status) {
        Optional<WeatherAlert> alertOpt = alertRepository.findById(alertId);
        if (alertOpt.isPresent()) {
            alertRepository.updateAlertStatus(alertId, status);

            // Update cache
            WeatherAlert alert = alertOpt.get();
            cacheLocationAlerts(alert.getLocation());

            log.info("Updated alert {} status to: {}", alertId, status);
        }
    }

    /**
     * Cancel an alert
     */
    public void cancelAlert(Long alertId, String reason) {
        updateAlertStatus(alertId, "CANCELLED");

        // Log cancellation reason
        String historyKey = ALERT_HISTORY_KEY + alertId;
        Map<String, Object> cancellation = new HashMap<>();
        cancellation.put("action", "CANCELLED");
        cancellation.put("reason", reason);
        cancellation.put("timestamp", LocalDateTime.now());

        redisTemplate.opsForList().leftPush(historyKey, cancellation);
        redisTemplate.expire(historyKey, 30, TimeUnit.DAYS);
    }

    /**
     * Check and process weather conditions for alerts
     */
    @Async
    public void checkWeatherConditionsForAlerts() {
        log.info("Checking weather conditions for potential alerts");

        List<Location> locations = locationRepository.findAll();

        for (Location location : locations) {
            try {
                Map<String, Object> currentWeather = externalWeatherApiService
                        .aggregateWeatherData(location.getName());

                processWeatherConditions(location, currentWeather);

            } catch (Exception e) {
                log.error("Error checking weather for location: {}", location.getName(), e);
            }
        }
    }

    /**
     * Scheduled task to update expired alerts
     */
    @Scheduled(fixedRate = 300000) // Every 5 minutes
    @Transactional
    public void updateExpiredAlerts() {
        log.debug("Updating expired alerts");

        int updatedCount = alertRepository.updateExpiredAlerts(LocalDateTime.now());

        if (updatedCount > 0) {
            log.info("Updated {} expired alerts", updatedCount);
        }
    }

    /**
     * Cleanup old alerts
     */
    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
    public void cleanupOldAlerts() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(30);

        try {
            alertRepository.deleteByCreatedAtBefore(cutoffDate);
            log.info("Cleaned up alerts older than: {}", cutoffDate);
        } catch (Exception e) {
            log.error("Error cleaning up old alerts", e);
        }
    }

    /**
     * Get alert statistics
     */
    public Map<String, Object> getAlertStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalActiveAlerts = alertRepository.countByLocationAndStatus(null, "ACTIVE");
        long criticalAlerts = alertRepository.countBySeverityAndStatus("CRITICAL", "ACTIVE");
        long highAlerts = alertRepository.countBySeverityAndStatus("HIGH", "ACTIVE");
        long mediumAlerts = alertRepository.countBySeverityAndStatus("MEDIUM", "ACTIVE");
        long lowAlerts = alertRepository.countBySeverityAndStatus("LOW", "ACTIVE");

        stats.put("totalActiveAlerts", totalActiveAlerts);
        stats.put("criticalAlerts", criticalAlerts);
        stats.put("highAlerts", highAlerts);
        stats.put("mediumAlerts", mediumAlerts);
        stats.put("lowAlerts", lowAlerts);
        stats.put("timestamp", LocalDateTime.now());

        return stats;
    }

    /**
     * Generate weather alerts based on current conditions
     */
    public void generateAutomaticAlerts(Location location, Map<String, Object> weatherData) {
        try {
            List<WeatherAlert> newAlerts = new ArrayList<>();

            // Check for extreme temperature
            if (weatherData.get("temperature") != null) {
                double temperature = ((Number) weatherData.get("temperature")).doubleValue();
                if (temperature > 40) {
                    newAlerts.add(createTemperatureAlert(location, temperature, "HIGH_TEMPERATURE"));
                } else if (temperature < -10) {
                    newAlerts.add(createTemperatureAlert(location, temperature, "LOW_TEMPERATURE"));
                }
            }

            // Check for high wind speeds
            if (weatherData.get("windSpeed") != null) {
                double windSpeed = ((Number) weatherData.get("windSpeed")).doubleValue();
                if (windSpeed > 50) { // 50 km/h
                    newAlerts.add(createWindAlert(location, windSpeed));
                }
            }

            // Check for severe weather conditions
            String condition = (String) weatherData.get("weatherCondition");
            if (condition != null) {
                if (condition.toLowerCase().contains("thunderstorm")) {
                    newAlerts.add(createSevereWeatherAlert(location, condition, "THUNDERSTORM"));
                } else if (condition.toLowerCase().contains("snow")) {
                    newAlerts.add(createSevereWeatherAlert(location, condition, "HEAVY_SNOW"));
                }
            }

            // Save new alerts
            for (WeatherAlert alert : newAlerts) {
                alertRepository.save(alert);
                processAlertNotifications(alert);
            }

            if (!newAlerts.isEmpty()) {
                log.info("Generated {} automatic alerts for location: {}", newAlerts.size(), location.getName());
                cacheLocationAlerts(location);
            }

        } catch (Exception e) {
            log.error("Error generating automatic alerts for location: {}", location.getName(), e);
        }
    }

    // Helper methods
    private void cacheLocationAlerts(Location location) {
        List<WeatherAlert> activeAlerts = alertRepository.findByLocationAndStatusOrderByAlertTimeDesc(location,
                "ACTIVE");
        String cacheKey = ALERT_CACHE_KEY + location.getId() + ":active";
        redisTemplate.opsForValue().set(cacheKey, activeAlerts, 5, TimeUnit.MINUTES);
    }

    private void processWeatherConditions(Location location, Map<String, Object> weatherData) {
        // Check if automatic alert generation is needed
        generateAutomaticAlerts(location, weatherData);
    }

    @Async
    private void processAlertNotifications(WeatherAlert alert) {
        // Here you would integrate with the notification module
        // For now, we'll just log and cache the notification requirement

        String notificationKey = "notifications:weather:alert:" + alert.getId();
        Map<String, Object> notification = new HashMap<>();
        notification.put("alertId", alert.getId());
        notification.put("locationId", alert.getLocation().getId());
        notification.put("severity", alert.getSeverity());
        notification.put("title", alert.getTitle());
        notification.put("description", alert.getDescription());
        notification.put("createdAt", LocalDateTime.now());

        redisTemplate.opsForValue().set(notificationKey, notification, 1, TimeUnit.HOURS);

        log.info("Prepared notification for alert: {} at location: {}",
                alert.getId(), alert.getLocation().getName());
    }

    private WeatherAlert createTemperatureAlert(Location location, double temperature, String alertType) {
        String severity = temperature > 45 || temperature < -20 ? "CRITICAL" : "HIGH";
        String title = alertType.equals("HIGH_TEMPERATURE") ? "Extreme High Temperature Warning"
                : "Extreme Low Temperature Warning";
        String description = String.format("Temperature has reached %.1f°C. Take appropriate precautions.",
                temperature);

        return new WeatherAlert(location, alertType, title, description, severity,
                LocalDateTime.now(), LocalDateTime.now().plusHours(6), "Auto-Generated");
    }

    private WeatherAlert createWindAlert(Location location, double windSpeed) {
        String severity = windSpeed > 75 ? "CRITICAL" : "HIGH";
        String title = "High Wind Speed Warning";
        String description = String.format(
                "Wind speed has reached %.1f km/h. Secure loose objects and avoid outdoor activities.", windSpeed);

        return new WeatherAlert(location, "HIGH_WIND", title, description, severity,
                LocalDateTime.now(), LocalDateTime.now().plusHours(4), "Auto-Generated");
    }

    private WeatherAlert createSevereWeatherAlert(Location location, String condition, String alertType) {
        String severity = alertType.equals("THUNDERSTORM") ? "HIGH" : "MEDIUM";
        String title = condition.contains("thunderstorm") ? "Thunderstorm Warning" : "Severe Weather Warning";
        String description = String.format("Severe weather conditions detected: %s. Exercise caution.", condition);

        return new WeatherAlert(location, alertType, title, description, severity,
                LocalDateTime.now(), LocalDateTime.now().plusHours(3), "Auto-Generated");
    }
}
