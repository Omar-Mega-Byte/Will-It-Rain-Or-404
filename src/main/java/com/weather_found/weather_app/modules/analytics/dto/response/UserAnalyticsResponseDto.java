package com.weather_found.weather_app.modules.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * DTO for user analytics dashboard response
 */
@Data
@Builder
public class UserAnalyticsResponseDto {
    private String username;
    private Long totalQueries;
    private Integer locationsSearched;
    private Double averageSessionTimeMinutes;
    private String mostSearchedLocation;
    private Long totalPredictions;
    private BigDecimal accuracyRate;
    private Instant lastActive;
    private SessionStatistics sessionStats;
    private List<ActivityTrend> activityTrends;
    private Map<String, Long> actionFrequency;
    private Map<Integer, Long> hourlyActivity;

    @Data
    @Builder
    public static class SessionStatistics {
        private Long totalSessions;
        private Double averageDurationMinutes;
        private Integer averagePageViews;
        private Integer averageActionsPerformed;
        private String preferredDeviceType;
        private String preferredBrowser;
    }

    @Data
    @Builder
    public static class ActivityTrend {
        private String date;
        private Long activityCount;
    }
}