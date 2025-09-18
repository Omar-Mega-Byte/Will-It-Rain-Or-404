package com.weather_found.weather_app.modules.analytics.repository;

import com.weather_found.weather_app.modules.analytics.model.ApiUsageLog;
import com.weather_found.weather_app.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface ApiUsageLogRepository extends JpaRepository<ApiUsageLog, Long> {

    /**
     * Find API usage by user within date range
     */
    List<ApiUsageLog> findByUserAndTimestampBetween(User user, Instant startDate, Instant endDate);

    /**
     * Find API usage by endpoint within date range
     */
    List<ApiUsageLog> findByEndpointAndTimestampBetween(String endpoint, Instant startDate, Instant endDate);

    /**
     * Count API calls by user within date range
     */
    @Query("SELECT COUNT(a) FROM ApiUsageLog a WHERE a.user = :user AND a.timestamp BETWEEN :startDate AND :endDate")
    Long countApiCallsByUser(@Param("user") User user, @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Get most popular endpoints
     */
    @Query("SELECT a.endpoint, COUNT(a) as count FROM ApiUsageLog a " +
            "WHERE a.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY a.endpoint ORDER BY count DESC")
    List<Object[]> findMostPopularEndpoints(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Get API response time statistics by endpoint
     */
    @Query("SELECT a.endpoint, AVG(a.responseTimeMs) as avgResponseTime, " +
            "MIN(a.responseTimeMs) as minResponseTime, MAX(a.responseTimeMs) as maxResponseTime " +
            "FROM ApiUsageLog a WHERE a.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY a.endpoint")
    List<Object[]> findResponseTimeStatistics(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Get error rate statistics
     */
    @Query("SELECT a.endpoint, " +
            "SUM(CASE WHEN a.responseStatus >= 400 THEN 1 ELSE 0 END) as errorCount, " +
            "COUNT(a) as totalCount, " +
            "(SUM(CASE WHEN a.responseStatus >= 400 THEN 1 ELSE 0 END) * 100.0 / COUNT(a)) as errorRate " +
            "FROM ApiUsageLog a WHERE a.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY a.endpoint")
    List<Object[]> findErrorRateStatistics(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Get hourly API usage patterns
     */
    @Query("SELECT HOUR(a.timestamp) as hour, COUNT(a) as count " +
            "FROM ApiUsageLog a WHERE a.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY HOUR(a.timestamp) ORDER BY hour")
    List<Object[]> findHourlyUsagePattern(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Get user API usage ranking
     */
    @Query("SELECT a.user.username, COUNT(a) as count " +
            "FROM ApiUsageLog a WHERE a.user IS NOT NULL " +
            "AND a.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY a.user.id, a.user.username ORDER BY count DESC")
    List<Object[]> findUserUsageRanking(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Get HTTP method usage statistics
     */
    @Query("SELECT a.httpMethod, COUNT(a) as count FROM ApiUsageLog a " +
            "WHERE a.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY a.httpMethod ORDER BY count DESC")
    List<Object[]> findHttpMethodUsage(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Find slow API calls (above threshold)
     */
    @Query("SELECT a FROM ApiUsageLog a WHERE a.responseTimeMs > :thresholdMs " +
            "AND a.timestamp BETWEEN :startDate AND :endDate " +
            "ORDER BY a.responseTimeMs DESC")
    List<ApiUsageLog> findSlowApiCalls(@Param("thresholdMs") Long thresholdMs,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Count unique users accessing API
     */
    @Query("SELECT COUNT(DISTINCT a.user) FROM ApiUsageLog a " +
            "WHERE a.user IS NOT NULL AND a.timestamp BETWEEN :startDate AND :endDate")
    Long countUniqueApiUsers(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}