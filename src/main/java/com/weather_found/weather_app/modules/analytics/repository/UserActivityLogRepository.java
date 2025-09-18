package com.weather_found.weather_app.modules.analytics.repository;

import com.weather_found.weather_app.modules.analytics.model.UserActivityLog;
import com.weather_found.weather_app.modules.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface UserActivityLogRepository extends JpaRepository<UserActivityLog, Long> {

    /**
     * Find activities by user within date range
     */
    List<UserActivityLog> findByUserAndTimestampBetween(User user, Instant startDate, Instant endDate);

    /**
     * Find activities by user and action
     */
    List<UserActivityLog> findByUserAndAction(User user, String action);

    /**
     * Count activities by user within date range
     */
    @Query("SELECT COUNT(u) FROM UserActivityLog u WHERE u.user = :user AND u.timestamp BETWEEN :startDate AND :endDate")
    Long countUserActivitiesBetween(@Param("user") User user, @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Get most frequent actions by user
     */
    @Query("SELECT u.action, COUNT(u) as count FROM UserActivityLog u WHERE u.user = :user " +
            "GROUP BY u.action ORDER BY count DESC")
    List<Object[]> findMostFrequentActionsByUser(@Param("user") User user);

    /**
     * Get user activity trends over time
     */
    @Query("SELECT DATE(u.timestamp) as date, COUNT(u) as count FROM UserActivityLog u " +
            "WHERE u.user = :user AND u.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(u.timestamp) ORDER BY date")
    List<Object[]> findUserActivityTrends(@Param("user") User user, @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Get system-wide activity statistics
     */
    @Query("SELECT u.action, COUNT(u) as count FROM UserActivityLog u " +
            "WHERE u.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY u.action ORDER BY count DESC")
    List<Object[]> findSystemActivityStatistics(@Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Find recent activities by user
     */
    Page<UserActivityLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);

    /**
     * Count unique users active within date range
     */
    @Query("SELECT COUNT(DISTINCT u.user) FROM UserActivityLog u WHERE u.timestamp BETWEEN :startDate AND :endDate")
    Long countUniqueActiveUsers(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Get hourly activity distribution for a user
     */
    @Query("SELECT HOUR(u.timestamp) as hour, COUNT(u) as count FROM UserActivityLog u " +
            "WHERE u.user = :user AND u.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY HOUR(u.timestamp) ORDER BY hour")
    List<Object[]> findUserActivityByHour(@Param("user") User user, @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Calculate average response time by action
     */
    @Query("SELECT u.action, AVG(u.responseTimeMs) as avgResponseTime FROM UserActivityLog u " +
            "WHERE u.responseTimeMs IS NOT NULL AND u.timestamp BETWEEN :startDate AND :endDate " +
            "GROUP BY u.action")
    List<Object[]> findAverageResponseTimeByAction(@Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);
}