package com.weather_found.weather_app.modules.weather.repository;

import com.weather_found.weather_app.modules.weather.model.Location;
import com.weather_found.weather_app.modules.weather.model.WeatherDataEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for WeatherData entities
 */
@Repository
public interface WeatherDataRepository extends JpaRepository<WeatherDataEntity, Long> {

    /**
     * Find latest weather data for a location
     */
    Optional<WeatherDataEntity> findTopByLocationOrderByRecordedAtDesc(Location location);

    /**
     * Find weather data for a location within a time range
     */
    List<WeatherDataEntity> findByLocationAndRecordedAtBetweenOrderByRecordedAtDesc(
            Location location, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find weather data for a location on a specific date
     */
    @Query("SELECT w FROM WeatherDataEntity w WHERE w.location = :location AND " +
            "DATE(w.recordedAt) = DATE(:date) ORDER BY w.recordedAt DESC")
    List<WeatherDataEntity> findByLocationAndDate(
            @Param("location") Location location,
            @Param("date") LocalDateTime date);

    /**
     * Find weather data by data source
     */
    List<WeatherDataEntity> findByDataSourceOrderByRecordedAtDesc(String dataSource);

    /**
     * Find weather data by location and data source
     */
    List<WeatherDataEntity> findByLocationAndDataSourceOrderByRecordedAtDesc(
            Location location, String dataSource);

    /**
     * Get weather data with pagination
     */
    Page<WeatherDataEntity> findByLocationOrderByRecordedAtDesc(Location location, Pageable pageable);

    /**
     * Find weather data within temperature range
     */
    List<WeatherDataEntity> findByTemperatureBetweenOrderByRecordedAtDesc(
            java.math.BigDecimal minTemp, java.math.BigDecimal maxTemp);

    /**
     * Find weather data with specific conditions
     */
    List<WeatherDataEntity> findByWeatherConditionContainingIgnoreCaseOrderByRecordedAtDesc(String condition);

    /**
     * Get average temperature for a location in a time period
     */
    @Query("SELECT AVG(w.temperature) FROM WeatherDataEntity w WHERE w.location = :location AND " +
            "w.recordedAt BETWEEN :startTime AND :endTime")
    java.math.BigDecimal getAverageTemperature(
            @Param("location") Location location,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Get weather statistics for a location
     */
    @Query("SELECT " +
            "AVG(w.temperature) as avgTemp, " +
            "MIN(w.temperature) as minTemp, " +
            "MAX(w.temperature) as maxTemp, " +
            "AVG(w.humidity) as avgHumidity, " +
            "AVG(w.pressure) as avgPressure " +
            "FROM WeatherDataEntity w WHERE w.location = :location AND " +
            "w.recordedAt BETWEEN :startTime AND :endTime")
    Object[] getWeatherStatistics(
            @Param("location") Location location,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    /**
     * Count weather records for a location
     */
    long countByLocation(Location location);

    /**
     * Find locations with recent weather data
     */
    @Query("SELECT DISTINCT w.location FROM WeatherDataEntity w WHERE " +
            "w.recordedAt >= :since ORDER BY w.location.name")
    List<Location> findLocationsWithRecentData(@Param("since") LocalDateTime since);

    /**
     * Delete old weather data (for cleanup)
     */
    void deleteByRecordedAtBefore(LocalDateTime cutoffDate);

    /**
     * Find weather data with high wind speeds
     */
    List<WeatherDataEntity> findByWindSpeedGreaterThanOrderByWindSpeedDesc(java.math.BigDecimal windSpeed);

    /**
     * Find weather data with precipitation
     */
    List<WeatherDataEntity> findByPrecipitationGreaterThanOrderByRecordedAtDesc(java.math.BigDecimal precipitation);

    /**
     * Get most recent weather data across all locations
     */
    @Query("SELECT w FROM WeatherDataEntity w WHERE w.recordedAt = " +
            "(SELECT MAX(w2.recordedAt) FROM WeatherDataEntity w2 WHERE w2.location = w.location) " +
            "ORDER BY w.location.name")
    List<WeatherDataEntity> findLatestWeatherForAllLocations();

    // Additional methods for dashboard service

    @Query("SELECT wd FROM WeatherDataEntity wd WHERE wd.location = :location AND wd.recordedAt >= :since ORDER BY wd.recordedAt DESC")
    List<WeatherDataEntity> findByLocationAndTimestampAfter(@Param("location") Location location,
            @Param("since") LocalDateTime since);

    @Query("SELECT wd FROM WeatherDataEntity wd WHERE wd.location = :location ORDER BY wd.recordedAt DESC")
    List<WeatherDataEntity> findByLocationOrderByTimestampDesc(@Param("location") Location location);

    @Query("SELECT wd FROM WeatherDataEntity wd WHERE wd.recordedAt >= :since ORDER BY wd.recordedAt DESC")
    List<WeatherDataEntity> findRecentWeatherData(@Param("since") LocalDateTime since);

    @Query("SELECT DATE(wd.recordedAt) as date, COUNT(wd) as count FROM WeatherDataEntity wd WHERE wd.recordedAt >= :since GROUP BY DATE(wd.recordedAt) ORDER BY date DESC")
    List<Object[]> getDailyDataCounts(@Param("since") LocalDateTime since);

    @Query("SELECT DATE(wd.recordedAt) as date, AVG(wd.temperature) as avgTemp FROM WeatherDataEntity wd WHERE wd.recordedAt >= :since GROUP BY DATE(wd.recordedAt) ORDER BY date DESC")
    List<Object[]> getTemperatureTrends(@Param("since") LocalDateTime since);

    @Query("SELECT DATE(wd.recordedAt) as date, AVG(wd.humidity) as avgHumidity FROM WeatherDataEntity wd WHERE wd.recordedAt >= :since GROUP BY DATE(wd.recordedAt) ORDER BY date DESC")
    List<Object[]> getHumidityTrends(@Param("since") LocalDateTime since);

    @Query("SELECT wd.weatherCondition as condition, COUNT(wd) as count FROM WeatherDataEntity wd WHERE wd.recordedAt >= :since GROUP BY wd.weatherCondition ORDER BY count DESC")
    List<Object[]> getPopularWeatherConditionsRaw(@Param("since") LocalDateTime since);

    @Query("SELECT AVG(wd.temperature) FROM WeatherDataEntity wd WHERE wd.location = :location AND wd.recordedAt >= :since")
    Double getAverageTemperatureByLocation(@Param("location") Location location, @Param("since") LocalDateTime since);

    @Query("SELECT MAX(wd.temperature) FROM WeatherDataEntity wd WHERE wd.location = :location AND wd.recordedAt >= :since")
    Double getMaxTemperatureByLocation(@Param("location") Location location, @Param("since") LocalDateTime since);

    @Query("SELECT MIN(wd.temperature) FROM WeatherDataEntity wd WHERE wd.location = :location AND wd.recordedAt >= :since")
    Double getMinTemperatureByLocation(@Param("location") Location location, @Param("since") LocalDateTime since);

    @Query("SELECT AVG(wd.temperature) FROM WeatherDataEntity wd WHERE wd.recordedAt >= :since")
    Double getGlobalAverageTemperature(@Param("since") LocalDateTime since);

    @Query("SELECT MAX(wd.temperature) FROM WeatherDataEntity wd WHERE wd.recordedAt >= :since")
    Double getGlobalMaxTemperature(@Param("since") LocalDateTime since);

    @Query("SELECT MIN(wd.temperature) FROM WeatherDataEntity wd WHERE wd.recordedAt >= :since")
    Double getGlobalMinTemperature(@Param("since") LocalDateTime since);

    @Query("SELECT COUNT(wd) FROM WeatherDataEntity wd WHERE wd.location = :location AND wd.recordedAt >= :since")
    long countByLocationAndTimestampAfter(@Param("location") Location location, @Param("since") LocalDateTime since);
}
