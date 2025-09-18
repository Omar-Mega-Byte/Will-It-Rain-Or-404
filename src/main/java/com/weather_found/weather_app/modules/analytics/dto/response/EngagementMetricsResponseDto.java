package com.weather_found.weather_app.modules.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for user engagement analytics response
 */
@Data
@Builder
public class EngagementMetricsResponseDto {
    private Long totalUsers;
    private ActiveUserStats activeUsers;
    private RegistrationStats newRegistrations;
    private RetentionStats userRetention;
    private List<FeatureUsage> popularFeatures;
    private Double averageSessionTimeMinutes;
    private GeographicStats geographicDistribution;
    private DeviceStats deviceUsage;
    private List<HourlyPattern> usagePatterns;

    @Data
    @Builder
    public static class ActiveUserStats {
        private Long today;
        private Long thisWeek;
        private Long thisMonth;
        private List<DailyActiveUsers> dailyTrends;
    }

    @Data
    @Builder
    public static class DailyActiveUsers {
        private String date;
        private Long activeUsers;
    }

    @Data
    @Builder
    public static class RegistrationStats {
        private Long today;
        private Long thisWeek;
        private Long thisMonth;
        private BigDecimal growthRate;
    }

    @Data
    @Builder
    public static class RetentionStats {
        private BigDecimal day1;
        private BigDecimal day7;
        private BigDecimal day30;
    }

    @Data
    @Builder
    public static class FeatureUsage {
        private String feature;
        private BigDecimal usagePercentage;
        private Long totalUsage;
    }

    @Data
    @Builder
    public static class GeographicStats {
        private Map<String, Long> topCountries;
        private Map<String, Long> topCities;
    }

    @Data
    @Builder
    public static class DeviceStats {
        private Map<String, BigDecimal> deviceTypes;
        private Map<String, BigDecimal> browsers;
        private Map<String, BigDecimal> operatingSystems;
    }

    @Data
    @Builder
    public static class HourlyPattern {
        private Integer hour;
        private Long sessionCount;
        private Double averageDuration;
    }
}