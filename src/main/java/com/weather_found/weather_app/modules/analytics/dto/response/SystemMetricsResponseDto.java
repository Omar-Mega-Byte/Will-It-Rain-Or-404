package com.weather_found.weather_app.modules.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for system performance metrics response
 */
@Data
@Builder
public class SystemMetricsResponseDto {
    private String serverHealth;
    private BigDecimal uptimePercentage;
    private Double averageResponseTimeMs;
    private BigDecimal memoryUsagePercentage;
    private BigDecimal cpuUsagePercentage;
    private BigDecimal diskUsagePercentage;
    private Long activeUsers;
    private Long dailyRequests;
    private BigDecimal errorRate;
    private Integer databaseConnections;
    private BigDecimal cacheHitRate;
    private Long apiCallsToday;
    private List<ComponentHealth> componentHealths;
    private List<PerformanceTrend> performanceTrends;
    private Map<String, BigDecimal> resourceUsage;

    @Data
    @Builder
    public static class ComponentHealth {
        private String componentName;
        private String status;
        private BigDecimal healthScore;
        private String lastChecked;
        private List<MetricValue> metrics;
    }

    @Data
    @Builder
    public static class MetricValue {
        private String metricName;
        private BigDecimal value;
        private String unit;
        private Boolean isHealthy;
    }

    @Data
    @Builder
    public static class PerformanceTrend {
        private String date;
        private String metricName;
        private BigDecimal value;
    }
}