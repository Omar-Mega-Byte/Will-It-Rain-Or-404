package com.weather_found.weather_app.modules.weather.repository;

import com.weather_found.weather_app.modules.weather.model.Location;
import com.weather_found.weather_app.modules.weather.model.WeatherPrediction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for WeatherPrediction entities
 */
@Repository
public interface WeatherPredictionRepository extends JpaRepository<WeatherPrediction, Long> {

    /**
     * Find predictions for a specific location
     */
    List<WeatherPrediction> findByLocationOrderByPredictedForAsc(Location location);

    /**
     * Find predictions for a location within a time range
     */
    List<WeatherPrediction> findByLocationAndPredictedForBetweenOrderByPredictedForAsc(
            Location location, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Find predictions for a specific event
     */
    List<WeatherPrediction> findByEventIdOrderByPredictedForAsc(Long eventId);

    /**
     * Find latest prediction for a location and time
     */
    @Query("SELECT p FROM WeatherPrediction p WHERE p.location = :location AND " +
            "p.predictedFor = :predictedFor ORDER BY p.predictionDate DESC")
    List<WeatherPrediction> findByLocationAndPredictedForOrderByPredictionDateDesc(
            @Param("location") Location location,
            @Param("predictedFor") LocalDateTime predictedFor);

    /**
     * Find most recent prediction for each future time slot for a location
     */
    @Query("SELECT p FROM WeatherPrediction p WHERE p.location = :location AND " +
            "p.predictedFor > :now AND p.predictionDate = " +
            "(SELECT MAX(p2.predictionDate) FROM WeatherPrediction p2 WHERE " +
            "p2.location = p.location AND p2.predictedFor = p.predictedFor) " +
            "ORDER BY p.predictedFor ASC")
    List<WeatherPrediction> findLatestPredictionsForLocation(
            @Param("location") Location location,
            @Param("now") LocalDateTime now);

    /**
     * Find predictions by model version
     */
    List<WeatherPrediction> findByModelVersionOrderByPredictionDateDesc(String modelVersion);

    /**
     * Find predictions with low confidence
     */
    List<WeatherPrediction> findByConfidenceScoreLessThanOrderByConfidenceScoreAsc(
            java.math.BigDecimal confidenceThreshold);

    /**
     * Find predictions that can be validated (past predictions)
     */
    List<WeatherPrediction> findByPredictedForBeforeAndPredictionAccuracyIsNullOrderByPredictedForDesc(
            LocalDateTime cutoffTime);

    /**
     * Update prediction accuracy
     */
    @Modifying
    @Query("UPDATE WeatherPrediction p SET p.predictionAccuracy = :accuracy WHERE p.id = :id")
    void updatePredictionAccuracy(@Param("id") Long id, @Param("accuracy") java.math.BigDecimal accuracy);

    /**
     * Get predictions with pagination
     */
    Page<WeatherPrediction> findByLocationOrderByPredictedForAsc(Location location, Pageable pageable);

    /**
     * Find predictions for multiple locations
     */
    List<WeatherPrediction> findByLocationInAndPredictedForBetweenOrderByLocationAscPredictedForAsc(
            List<Location> locations, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Get average accuracy for a model version
     */
    @Query("SELECT AVG(p.predictionAccuracy) FROM WeatherPrediction p WHERE " +
            "p.modelVersion = :modelVersion AND p.predictionAccuracy IS NOT NULL")
    java.math.BigDecimal getAverageAccuracyForModel(@Param("modelVersion") String modelVersion);

    /**
     * Count predictions by location
     */
    long countByLocation(Location location);

    /**
     * Count predictions by event
     */
    long countByEventId(Long eventId);

    /**
     * Find predictions with high precipitation probability
     */
    List<WeatherPrediction> findByPrecipitationProbabilityGreaterThanOrderByPrecipitationProbabilityDesc(
            java.math.BigDecimal threshold);

    /**
     * Find predictions for extreme temperatures
     */
    @Query("SELECT p FROM WeatherPrediction p WHERE " +
            "p.temperatureMin < :coldThreshold OR p.temperatureMax > :hotThreshold " +
            "ORDER BY p.predictedFor ASC")
    List<WeatherPrediction> findExtremeTemperaturePredictions(
            @Param("coldThreshold") java.math.BigDecimal coldThreshold,
            @Param("hotThreshold") java.math.BigDecimal hotThreshold);

    /**
     * Delete old predictions
     */
    void deleteByPredictionDateBefore(LocalDateTime cutoffDate);

    /**
     * Find predictions for today
     */
    @Query("SELECT p FROM WeatherPrediction p WHERE " +
            "DATE(p.predictedFor) = DATE(:today) ORDER BY p.location.name, p.predictedFor ASC")
    List<WeatherPrediction> findPredictionsForToday(@Param("today") LocalDateTime today);

    /**
     * Find event predictions for a specific user (assuming event ownership)
     */
    @Query("SELECT p FROM WeatherPrediction p WHERE p.eventId IN :eventIds ORDER BY p.predictedFor ASC")
    List<WeatherPrediction> findByEventIds(@Param("eventIds") List<Long> eventIds);
}
