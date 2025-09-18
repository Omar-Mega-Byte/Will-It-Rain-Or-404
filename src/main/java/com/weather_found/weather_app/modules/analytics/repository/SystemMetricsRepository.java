package com.weather_found.weather_app.modules.analytics.repository;

import com.weather_found.weather_app.modules.analytics.model.SystemMetrics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface SystemMetricsRepository extends JpaRepository<SystemMetrics, Long> {

    /**
     * Find metrics by component within date range
     */
    List<SystemMetrics> findByComponentNameAndTimestampBetween(String componentName, Instant startDate,
            Instant endDate);

    /**
     * Find metrics by type within date range
     */
    List<SystemMetrics> findByMetricTypeAndTimestampBetween(String metricType, Instant startDate, Instant endDate);

    /**
     * Get latest metrics by component
     */
    @Query("SELECT s FROM SystemMetrics s WHERE s.componentName = :componentName " +
            "AND s.timestamp = (SELECT MAX(s2.timestamp) FROM SystemMetrics s2 WHERE s2.componentName = :componentName)")
    List<SystemMetrics> findLatestMetricsByComponent(@Param("componentName") String componentName);

    /**
     * Get average metric values by component and type
     */
    @Query("SELECT s.componentName, s.metricName, AVG(s.metricValue) as avgValue " +
            "FROM SystemMetrics s WHERE s.metricType = :metricType " +
            "AND s.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY s.componentName, s.metricName")
    List<Object[]> findAverageMetricsByType(@Param("metricType") String metricType,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Get metric trends over time
     */
    @Query("SELECT DATE(s.timestamp) as date, s.metricName, AVG(s.metricValue) as avgValue " +
            "FROM SystemMetrics s WHERE s.componentName = :componentName " +
            "AND s.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(s.timestamp), s.metricName ORDER BY date")
    List<Object[]> findMetricTrends(@Param("componentName") String componentName,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Find unhealthy metrics
     */
    List<SystemMetrics> findByIsHealthyFalseAndTimestampBetween(Instant startDate, Instant endDate);

    /**
     * Get system health summary
     */
    @Query("SELECT s.componentName, " +
            "SUM(CASE WHEN s.isHealthy = true THEN 1 ELSE 0 END) as healthyCount, " +
            "SUM(CASE WHEN s.isHealthy = false THEN 1 ELSE 0 END) as unhealthyCount " +
            "FROM SystemMetrics s WHERE s.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY s.componentName")
    List<Object[]> findSystemHealthSummary(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Get peak resource usage
     */
    @Query("SELECT s.componentName, s.metricName, MAX(s.metricValue) as maxValue " +
            "FROM SystemMetrics s WHERE s.metricType = 'RESOURCE' " +
            "AND s.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY s.componentName, s.metricName")
    List<Object[]> findPeakResourceUsage(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Get performance metrics summary
     */
    @Query("SELECT s.metricName, MIN(s.metricValue) as minValue, " +
            "MAX(s.metricValue) as maxValue, AVG(s.metricValue) as avgValue " +
            "FROM SystemMetrics s WHERE s.metricType = 'PERFORMANCE' " +
            "AND s.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY s.metricName")
    List<Object[]> findPerformanceSummary(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Count metrics by component and health status
     */
    @Query("SELECT s.componentName, s.isHealthy, COUNT(s) " +
            "FROM SystemMetrics s WHERE s.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY s.componentName, s.isHealthy")
    List<Object[]> countMetricsByComponentAndHealth(@Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}