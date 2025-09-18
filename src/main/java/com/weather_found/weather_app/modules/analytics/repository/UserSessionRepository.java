package com.weather_found.weather_app.modules.analytics.repository;

import com.weather_found.weather_app.modules.analytics.model.UserSession;
import com.weather_found.weather_app.modules.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Long> {

    /**
     * Find active session by session ID
     */
    Optional<UserSession> findBySessionIdAndIsActiveTrue(String sessionId);

    /**
     * Find sessions by user within date range
     */
    List<UserSession> findByUserAndSessionStartBetween(User user, Instant startDate, Instant endDate);

    /**
     * Find active sessions by user
     */
    List<UserSession> findByUserAndIsActiveTrue(User user);

    /**
     * Calculate average session duration by user
     */
    @Query("SELECT AVG(s.durationSeconds) FROM UserSession s WHERE s.user = :user " +
            "AND s.durationSeconds IS NOT NULL AND s.sessionStart BETWEEN :startDate AND :endDate")
    Double findAverageSessionDurationByUser(@Param("user") User user,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Get user engagement statistics
     */
    @Query("SELECT COUNT(s) as sessionCount, AVG(s.durationSeconds) as avgDuration, " +
            "AVG(s.pageViews) as avgPageViews, AVG(s.actionsPerformed) as avgActions " +
            "FROM UserSession s WHERE s.user = :user " +
            "AND s.sessionStart BETWEEN :startDate AND :endDate")
    List<Object[]> findUserEngagementStats(@Param("user") User user,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Get device type usage statistics
     */
    @Query("SELECT s.deviceType, COUNT(s) as count FROM UserSession s " +
            "WHERE s.sessionStart BETWEEN :startDate AND :endDate " +
            "GROUP BY s.deviceType ORDER BY count DESC")
    List<Object[]> findDeviceTypeUsage(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Get browser usage statistics
     */
    @Query("SELECT s.browser, COUNT(s) as count FROM UserSession s " +
            "WHERE s.sessionStart BETWEEN :startDate AND :endDate " +
            "GROUP BY s.browser ORDER BY count DESC")
    List<Object[]> findBrowserUsage(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Get geographic distribution of sessions
     */
    @Query("SELECT s.country, s.city, COUNT(s) as count FROM UserSession s " +
            "WHERE s.sessionStart BETWEEN :startDate AND :endDate " +
            "GROUP BY s.country, s.city ORDER BY count DESC")
    List<Object[]> findGeographicDistribution(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Count active sessions at given time
     */
    @Query("SELECT COUNT(s) FROM UserSession s WHERE s.sessionStart <= :timestamp " +
            "AND (s.sessionEnd IS NULL OR s.sessionEnd >= :timestamp)")
    Long countActiveSessionsAt(@Param("timestamp") Instant timestamp);

    /**
     * Find sessions with high engagement (above threshold)
     */
    @Query("SELECT s FROM UserSession s WHERE s.durationSeconds > :durationThreshold " +
            "OR s.pageViews > :pageViewThreshold OR s.actionsPerformed > :actionThreshold " +
            "AND s.sessionStart BETWEEN :startDate AND :endDate " +
            "ORDER BY s.durationSeconds DESC")
    List<UserSession> findHighEngagementSessions(@Param("durationThreshold") Long durationThreshold,
            @Param("pageViewThreshold") Integer pageViewThreshold,
            @Param("actionThreshold") Integer actionThreshold,
            @Param("startDate") Instant startDate,
            @Param("endDate") Instant endDate);

    /**
     * Get session patterns by hour of day
     */
    @Query("SELECT HOUR(s.sessionStart) as hour, COUNT(s) as count, AVG(s.durationSeconds) as avgDuration " +
            "FROM UserSession s WHERE s.sessionStart BETWEEN :startDate AND :endDate " +
            "GROUP BY HOUR(s.sessionStart) ORDER BY hour")
    List<Object[]> findSessionPatternsByHour(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);

    /**
     * Count unique daily users
     */
    @Query("SELECT DATE(s.sessionStart) as date, COUNT(DISTINCT s.user) as uniqueUsers " +
            "FROM UserSession s WHERE s.sessionStart BETWEEN :startDate AND :endDate " +
            "GROUP BY DATE(s.sessionStart) ORDER BY date")
    List<Object[]> findDailyActiveUsers(@Param("startDate") Instant startDate, @Param("endDate") Instant endDate);
}