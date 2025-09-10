package com.weather_found.weather_app.modules.weather.repository;

import com.weather_found.weather_app.modules.weather.model.Location;
import com.weather_found.weather_app.modules.weather.model.WeatherAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for WeatherAlert entities
 */
@Repository
public interface WeatherAlertRepository extends JpaRepository<WeatherAlert, Long> {

    /**
     * Find active alerts for a location
     */
    List<WeatherAlert> findByLocationAndStatusOrderByAlertTimeDesc(Location location, String status);

    /**
     * Find alerts by severity level
     */
    List<WeatherAlert> findBySeverityAndStatusOrderByAlertTimeDesc(String severity, String status);

    /**
     * Find alerts by type
     */
    List<WeatherAlert> findByAlertTypeAndStatusOrderByAlertTimeDesc(String alertType, String status);

    /**
     * Find active alerts for multiple locations
     */
    List<WeatherAlert> findByLocationInAndStatusOrderByAlertTimeDesc(List<Location> locations, String status);

    /**
     * Find alerts that are expiring soon
     */
    @Query("SELECT a FROM WeatherAlert a WHERE a.status = 'ACTIVE' AND " +
            "a.expiresAt BETWEEN :now AND :expiryThreshold ORDER BY a.expiresAt ASC")
    List<WeatherAlert> findExpiringSoon(
            @Param("now") LocalDateTime now,
            @Param("expiryThreshold") LocalDateTime expiryThreshold);

    /**
     * Find expired alerts that need to be updated
     */
    @Query("SELECT a FROM WeatherAlert a WHERE a.status = 'ACTIVE' AND a.expiresAt < :now")
    List<WeatherAlert> findExpiredActiveAlerts(@Param("now") LocalDateTime now);

    /**
     * Update alert status
     */
    @Modifying
    @Query("UPDATE WeatherAlert a SET a.status = :status WHERE a.id = :id")
    void updateAlertStatus(@Param("id") Long id, @Param("status") String status);

    /**
     * Update expired alerts to EXPIRED status
     */
    @Modifying
    @Query("UPDATE WeatherAlert a SET a.status = 'EXPIRED' WHERE a.status = 'ACTIVE' AND a.expiresAt < :now")
    int updateExpiredAlerts(@Param("now") LocalDateTime now);

    /**
     * Find alerts within a time range
     */
    List<WeatherAlert> findByAlertTimeBetweenOrderByAlertTimeDesc(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find critical alerts
     */
    @Query("SELECT a FROM WeatherAlert a WHERE a.severity = 'CRITICAL' AND a.status = :status ORDER BY a.alertTime DESC")
    List<WeatherAlert> findCriticalAlerts(@Param("status") String status);

    /**
     * Count active alerts by location
     */
    long countByLocationAndStatus(Location location, String status);

    /**
     * Count alerts by severity
     */
    long countBySeverityAndStatus(String severity, String status);

    /**
     * Find alerts by external ID
     */
    WeatherAlert findByExternalAlertId(String externalAlertId);

    /**
     * Find recent alerts (last 24 hours)
     */
    @Query("SELECT a FROM WeatherAlert a WHERE a.alertTime >= :since ORDER BY a.alertTime DESC")
    List<WeatherAlert> findRecentAlerts(@Param("since") LocalDateTime since);

    /**
     * Find all active alerts across all locations
     */
    List<WeatherAlert> findByStatusOrderByAlertTimeDesc(String status);

    /**
     * Delete old alerts
     */
    void deleteByCreatedAtBefore(LocalDateTime cutoffDate);

    /**
     * Find alerts by data source
     */
    List<WeatherAlert> findByDataSourceAndStatusOrderByAlertTimeDesc(String dataSource, String status);
}
