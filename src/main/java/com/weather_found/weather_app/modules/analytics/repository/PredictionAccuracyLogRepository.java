package com.weather_found.weather_app.modules.analytics.repository;

import com.weather_found.weather_app.modules.analytics.model.PredictionAccuracyLog;
import com.weather_found.weather_app.modules.weather.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface PredictionAccuracyLogRepository extends JpaRepository<PredictionAccuracyLog, Long> {

    /**
     * Find predictions by location and date range
     */
    List<PredictionAccuracyLog> findByLocationAndPredictionDateBetween(Location location, Instant startDate,
            Instant endDate);

    /**
     * Find verified predictions within date range
     */
    List<PredictionAccuracyLog> findByIsVerifiedTrueAndPredictionDateBetween(Instant startDate, Instant endDate);

    /**
     * Calculate overall accuracy by prediction type
     */
    @Query("SELECT p.predictionType, AVG(p.accuracyScore) as avgAccuracy, COUNT(p) as count " +
            "FROM PredictionAccuracyLog p WHERE p.isVerified = true " +
            "AND p.predictionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.predictionType")
    List<Object[]> findAccuracyByPredictionType(@Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Calculate accuracy by model version
     */
    @Query("SELECT p.modelVersion, AVG(p.accuracyScore) as avgAccuracy, COUNT(p) as count " +
            "FROM PredictionAccuracyLog p WHERE p.isVerified = true " +
            "AND p.predictionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.modelVersion ORDER BY avgAccuracy DESC")
    List<Object[]> findAccuracyByModelVersion(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Get accuracy trends over time
     */
    @Query("SELECT DATE(p.predictionDate) as date, AVG(p.accuracyScore) as avgAccuracy " +
            "FROM PredictionAccuracyLog p WHERE p.isVerified = true " +
            "AND p.predictionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(p.predictionDate) ORDER BY date")
    List<Object[]> findAccuracyTrends(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Calculate average response time by prediction type
     */
    @Query("SELECT p.predictionType, AVG(p.responseTimeMs) as avgResponseTime " +
            "FROM PredictionAccuracyLog p WHERE p.responseTimeMs IS NOT NULL " +
            "AND p.predictionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.predictionType")
    List<Object[]> findAverageResponseTimeByType(@Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Find predictions with accuracy below threshold
     */
    @Query("SELECT p FROM PredictionAccuracyLog p WHERE p.accuracyScore < :threshold " +
            "AND p.isVerified = true AND p.predictionDate BETWEEN :startDate AND :endDate " +
            "ORDER BY p.accuracyScore ASC")
    List<PredictionAccuracyLog> findLowAccuracyPredictions(@Param("threshold") BigDecimal threshold,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Count total predictions by model version
     */
    @Query("SELECT p.modelVersion, COUNT(p) FROM PredictionAccuracyLog p " +
            "WHERE p.predictionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.modelVersion")
    List<Object[]> countPredictionsByModelVersion(@Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Get location-wise accuracy statistics
     */
    @Query("SELECT p.location.name, AVG(p.accuracyScore) as avgAccuracy, COUNT(p) as count " +
            "FROM PredictionAccuracyLog p WHERE p.isVerified = true " +
            "AND p.predictionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.location.id, p.location.name " +
            "ORDER BY avgAccuracy DESC")
    List<Object[]> findLocationAccuracyStatistics(@Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Get confidence vs accuracy correlation
     */
    @Query("SELECT p.confidenceScore, AVG(p.accuracyScore) as avgAccuracy " +
            "FROM PredictionAccuracyLog p WHERE p.isVerified = true " +
            "AND p.confidenceScore IS NOT NULL AND p.predictionDate BETWEEN :startDate AND :endDate " +
            "GROUP BY p.confidenceScore ORDER BY p.confidenceScore")
    List<Object[]> findConfidenceAccuracyCorrelation(@Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}