package com.weather_found.weather_app.modules.analytics.service;

import com.weather_found.weather_app.modules.analytics.dto.response.UserAnalyticsResponseDto;
import com.weather_found.weather_app.modules.analytics.model.UserActivityLog;
import com.weather_found.weather_app.modules.analytics.model.UserSession;
import com.weather_found.weather_app.modules.analytics.repository.UserActivityLogRepository;
import com.weather_found.weather_app.modules.analytics.repository.UserSessionRepository;
import com.weather_found.weather_app.modules.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for tracking and analyzing user behavior
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@ConditionalOnProperty(name = "app.analytics.enabled", havingValue = "true", matchIfMissing = true)
public class UserAnalyticsService {

    private final UserActivityLogRepository activityLogRepository;
    private final UserSessionRepository sessionRepository;

    /**
     * Record user activity for analytics
     */
    @Transactional
    public void recordActivity(User user, String action, String entityType, Long entityId,
            String ipAddress, String userAgent, String sessionId,
            String details, Long responseTimeMs) {
        try {
            UserActivityLog activityLog = new UserActivityLog();
            // The UserActivityLog entity does not expose setUser(User); avoid calling it
            // here.
            // If you need to associate the user, set an identifier field on UserActivityLog
            // (e.g. userId)
            // or update the UserActivityLog entity to include a User relationship with a
            // corresponding setter.
            activityLog.setAction(action);
            activityLog.setEntityType(entityType);
            activityLog.setEntityId(entityId);
            activityLog.setIpAddress(ipAddress);
            activityLog.setUserAgent(userAgent);
            activityLog.setSessionId(sessionId);
            activityLog.setDetails(details);
            activityLog.setResponseTimeMs(responseTimeMs);

            activityLogRepository.save(activityLog);
            log.debug("Recorded activity: {} for user: {}", action, user.getUsername());
        } catch (Exception e) {
            log.error("Failed to record user activity: {}", e.getMessage(), e);
        }
    }

    /**
     * Get comprehensive user analytics
     */
    public UserAnalyticsResponseDto getUserAnalytics(User user, int daysBack) {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(daysBack, ChronoUnit.DAYS);

        // Get basic activity statistics
        Long totalQueries = activityLogRepository.countUserActivitiesBetween(user, startDate, endDate);

        // Get most frequent actions
        List<Object[]> actionFrequencyData = activityLogRepository.findMostFrequentActionsByUser(user);
        Map<String, Long> actionFrequency = actionFrequencyData.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> ((Number) row[1]).longValue()));

        // Get activity trends
        List<Object[]> trendsData = activityLogRepository.findUserActivityTrends(user, startDate, endDate);
        List<UserAnalyticsResponseDto.ActivityTrend> trends = trendsData.stream()
                .map(row -> UserAnalyticsResponseDto.ActivityTrend.builder()
                        .date(row[0].toString())
                        .activityCount(((Number) row[1]).longValue())
                        .build())
                .collect(Collectors.toList());

        // Get hourly activity pattern
        List<Object[]> hourlyData = activityLogRepository.findUserActivityByHour(user, startDate, endDate);
        Map<Integer, Long> hourlyActivity = hourlyData.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).intValue(),
                        row -> ((Number) row[1]).longValue()));

        // Get session statistics
        List<Object[]> sessionStatsData = sessionRepository.findUserEngagementStats(user, startDate, endDate);
        UserAnalyticsResponseDto.SessionStatistics sessionStats = null;
        if (!sessionStatsData.isEmpty()) {
            Object[] stats = sessionStatsData.get(0);
            sessionStats = UserAnalyticsResponseDto.SessionStatistics.builder()
                    .totalSessions(((Number) stats[0]).longValue())
                    .averageDurationMinutes(stats[1] != null ? ((Number) stats[1]).doubleValue() / 60.0 : 0.0)
                    .averagePageViews(stats[2] != null ? ((Number) stats[2]).intValue() : 0)
                    .averageActionsPerformed(stats[3] != null ? ((Number) stats[3]).intValue() : 0)
                    .build();
        }

        // Find latest activity
        List<UserActivityLog> recentActivities = activityLogRepository
                .findByUserOrderByTimestampDesc(user, org.springframework.data.domain.PageRequest.of(0, 1))
                .getContent();
        Instant lastActive = recentActivities.isEmpty() ? null : recentActivities.get(0).getTimestamp();

        return UserAnalyticsResponseDto.builder()
                .username(user.getUsername())
                .totalQueries(totalQueries)
                .locationsSearched(calculateUniqueLocations(user, startDate, endDate))
                .averageSessionTimeMinutes(sessionStats != null ? sessionStats.getAverageDurationMinutes() : 0.0)
                .totalPredictions(countPredictionRequests(user, startDate, endDate))
                .lastActive(lastActive)
                .sessionStats(sessionStats)
                .activityTrends(trends)
                .actionFrequency(actionFrequency)
                .hourlyActivity(hourlyActivity)
                .build();
    }

    /**
     * Start a new user session
     */
    @Transactional
    public UserSession startSession(User user, String sessionId, String ipAddress, String userAgent) {
        try {
            UserSession session = new UserSession();
            session.setUser(user);
            session.setSessionId(sessionId);
            session.setIpAddress(ipAddress);
            session.setUserAgent(userAgent);
            session.setDeviceType(extractDeviceType(userAgent));
            session.setBrowser(extractBrowser(userAgent));
            session.setOperatingSystem(extractOS(userAgent));

            return sessionRepository.save(session);
        } catch (Exception e) {
            log.error("Failed to start session for user: {}", user.getUsername(), e);
            return null;
        }
    }

    /**
     * End user session
     */
    @Transactional
    public void endSession(String sessionId) {
        try {
            sessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                    .ifPresent(session -> {
                        session.endSession();
                        sessionRepository.save(session);
                    });
        } catch (Exception e) {
            log.error("Failed to end session: {}", sessionId, e);
        }
    }

    /**
     * Update session activity counters
     */
    @Transactional
    public void updateSessionActivity(String sessionId, boolean pageView, boolean action) {
        try {
            sessionRepository.findBySessionIdAndIsActiveTrue(sessionId)
                    .ifPresent(session -> {
                        if (pageView) {
                            session.setPageViews(session.getPageViews() + 1);
                        }
                        if (action) {
                            session.setActionsPerformed(session.getActionsPerformed() + 1);
                        }
                        sessionRepository.save(session);
                    });
        } catch (Exception e) {
            log.error("Failed to update session activity: {}", sessionId, e);
        }
    }

    private Integer calculateUniqueLocations(User user, Instant startDate, Instant endDate) {
        // Count unique locations from weather query activities
        List<UserActivityLog> weatherQueries = activityLogRepository
                .findByUserAndTimestampBetween(user, startDate, endDate)
                .stream()
                .filter(log -> "WEATHER_QUERY".equals(log.getAction()))
                .collect(Collectors.toList());

        return (int) weatherQueries.stream()
                .filter(log -> log.getEntityId() != null)
                .map(UserActivityLog::getEntityId)
                .distinct()
                .count();
    }

    private Long countPredictionRequests(User user, Instant startDate, Instant endDate) {
        return activityLogRepository.findByUserAndTimestampBetween(user, startDate, endDate)
                .stream()
                .filter(log -> "PREDICTION_REQUEST".equals(log.getAction()))
                .count();
    }

    private String extractDeviceType(String userAgent) {
        if (userAgent == null)
            return "Unknown";
        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("mobile") || userAgent.contains("android") || userAgent.contains("iphone")) {
            return "Mobile";
        } else if (userAgent.contains("tablet") || userAgent.contains("ipad")) {
            return "Tablet";
        } else {
            return "Desktop";
        }
    }

    private String extractBrowser(String userAgent) {
        if (userAgent == null)
            return "Unknown";
        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("chrome"))
            return "Chrome";
        if (userAgent.contains("firefox"))
            return "Firefox";
        if (userAgent.contains("safari"))
            return "Safari";
        if (userAgent.contains("edge"))
            return "Edge";
        if (userAgent.contains("opera"))
            return "Opera";
        return "Other";
    }

    private String extractOS(String userAgent) {
        if (userAgent == null)
            return "Unknown";
        userAgent = userAgent.toLowerCase();

        if (userAgent.contains("windows"))
            return "Windows";
        if (userAgent.contains("mac"))
            return "macOS";
        if (userAgent.contains("linux"))
            return "Linux";
        if (userAgent.contains("android"))
            return "Android";
        if (userAgent.contains("ios"))
            return "iOS";
        return "Other";
    }
}