package com.weather_found.weather_app.modules.analytics.service;

import com.weather_found.weather_app.modules.analytics.dto.response.SystemMetricsResponseDto;
import com.weather_found.weather_app.modules.analytics.model.SystemMetrics;
import com.weather_found.weather_app.modules.analytics.repository.SystemMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service for tracking and analyzing system performance metrics
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SystemMetricsService {

    private final SystemMetricsRepository systemMetricsRepository;

    /**
     * Record system metric
     */
    @Transactional
    public void recordMetric(String metricType, String componentName, String metricName,
            BigDecimal metricValue, String unit, String tags,
            BigDecimal thresholdMin, BigDecimal thresholdMax) {
        try {
            SystemMetrics metric = new SystemMetrics();
            metric.setMetricType(metricType);
            metric.setComponentName(componentName);
            metric.setMetricName(metricName);
            metric.setMetricValue(metricValue);
            metric.setUnit(unit);
            metric.setTags(tags);
            metric.setThresholdMin(thresholdMin);
            metric.setThresholdMax(thresholdMax);

            // Determine if metric is healthy
            boolean isHealthy = true;
            if (thresholdMin != null && metricValue.compareTo(thresholdMin) < 0) {
                isHealthy = false;
            }
            if (thresholdMax != null && metricValue.compareTo(thresholdMax) > 0) {
                isHealthy = false;
            }
            metric.setIsHealthy(isHealthy);

            systemMetricsRepository.save(metric);
            log.debug("Recorded system metric: {}.{} = {}", componentName, metricName, metricValue);
        } catch (Exception e) {
            log.error("Failed to record system metric: {}", e.getMessage(), e);
        }
    }

    /**
     * Get comprehensive system metrics
     */
    public SystemMetricsResponseDto getSystemMetrics(int hoursBack) {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(hoursBack, ChronoUnit.HOURS);

        // Get component health summary
        List<Object[]> healthData = systemMetricsRepository.findSystemHealthSummary(startDate, endDate);
        List<SystemMetricsResponseDto.ComponentHealth> componentHealths = healthData.stream()
                .map(row -> {
                    String componentName = (String) row[0];
                    Long healthyCount = ((Number) row[1]).longValue();
                    Long unhealthyCount = ((Number) row[2]).longValue();

                    BigDecimal healthScore = BigDecimal.valueOf(healthyCount)
                            .divide(BigDecimal.valueOf(healthyCount + unhealthyCount), 2, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));

                    String status = healthScore.compareTo(BigDecimal.valueOf(90)) >= 0 ? "Excellent"
                            : healthScore.compareTo(BigDecimal.valueOf(70)) >= 0 ? "Good"
                                    : healthScore.compareTo(BigDecimal.valueOf(50)) >= 0 ? "Warning" : "Critical";

                    List<SystemMetricsResponseDto.MetricValue> latestMetrics = getLatestComponentMetrics(componentName);

                    return SystemMetricsResponseDto.ComponentHealth.builder()
                            .componentName(componentName)
                            .status(status)
                            .healthScore(healthScore)
                            .lastChecked(endDate.toString())
                            .metrics(latestMetrics)
                            .build();
                })
                .collect(Collectors.toList());

        // Get performance trends
        List<Object[]> trendsData = systemMetricsRepository.findMetricTrends("API", startDate, endDate);
        List<SystemMetricsResponseDto.PerformanceTrend> performanceTrends = trendsData.stream()
                .map(row -> SystemMetricsResponseDto.PerformanceTrend.builder()
                        .date(row[0].toString())
                        .metricName((String) row[1])
                        .value((BigDecimal) row[2])
                        .build())
                .collect(Collectors.toList());

        // Get resource usage
        List<Object[]> resourceData = systemMetricsRepository.findPeakResourceUsage(startDate, endDate);
        Map<String, BigDecimal> resourceUsage = resourceData.stream()
                .collect(Collectors.toMap(
                        row -> row[0] + "." + row[1],
                        row -> (BigDecimal) row[2]));

        // Calculate overall metrics
        BigDecimal overallHealth = componentHealths.stream()
                .map(SystemMetricsResponseDto.ComponentHealth::getHealthScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, componentHealths.size())), 2, RoundingMode.HALF_UP);

        String serverHealth = overallHealth.compareTo(BigDecimal.valueOf(90)) >= 0 ? "Excellent"
                : overallHealth.compareTo(BigDecimal.valueOf(70)) >= 0 ? "Good"
                        : overallHealth.compareTo(BigDecimal.valueOf(50)) >= 0 ? "Warning" : "Critical";

        return SystemMetricsResponseDto.builder()
                .serverHealth(serverHealth)
                .uptimePercentage(overallHealth)
                .averageResponseTimeMs(calculateAverageResponseTime(startDate, endDate))
                .memoryUsagePercentage(getLatestMetricValue("JVM", "memory_usage"))
                .cpuUsagePercentage(getLatestMetricValue("JVM", "cpu_usage"))
                .diskUsagePercentage(getLatestMetricValue("SYSTEM", "disk_usage"))
                .activeUsers(getActiveUsersCount())
                .dailyRequests(getDailyRequestsCount())
                .errorRate(calculateErrorRate(startDate, endDate))
                .databaseConnections(getDatabaseConnectionsCount())
                .cacheHitRate(getCacheHitRate())
                .apiCallsToday(getApiCallsToday())
                .componentHealths(componentHealths)
                .performanceTrends(performanceTrends)
                .resourceUsage(resourceUsage)
                .build();
    }

    /**
     * Record application startup metrics
     */
    @Transactional
    public void recordStartupMetrics() {
        recordMetric("SYSTEM", "APPLICATION", "startup_time",
                BigDecimal.valueOf(System.currentTimeMillis()), "ms", null, null, null);
        recordMetric("SYSTEM", "APPLICATION", "status", BigDecimal.ONE, "boolean", null, null, null);
    }

    /**
     * Record JVM metrics
     */
    @Transactional
    public void recordJvmMetrics() {
        Runtime runtime = Runtime.getRuntime();

        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        BigDecimal memoryUsagePercent = BigDecimal.valueOf(usedMemory)
                .divide(BigDecimal.valueOf(totalMemory), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        recordMetric("RESOURCE", "JVM", "memory_usage", memoryUsagePercent, "%",
                null, null, BigDecimal.valueOf(85));
        recordMetric("RESOURCE", "JVM", "total_memory", BigDecimal.valueOf(totalMemory), "bytes",
                null, null, null);
        recordMetric("RESOURCE", "JVM", "free_memory", BigDecimal.valueOf(freeMemory), "bytes",
                null, null, null);
    }

    private List<SystemMetricsResponseDto.MetricValue> getLatestComponentMetrics(String componentName) {
        List<SystemMetrics> latestMetrics = systemMetricsRepository.findLatestMetricsByComponent(componentName);

        return latestMetrics.stream()
                .map(metric -> SystemMetricsResponseDto.MetricValue.builder()
                        .metricName(metric.getMetricName())
                        .value(metric.getMetricValue())
                        .unit(metric.getUnit())
                        .isHealthy(metric.getIsHealthy())
                        .build())
                .collect(Collectors.toList());
    }

    private BigDecimal getLatestMetricValue(String componentName, String metricName) {
        List<SystemMetrics> metrics = systemMetricsRepository.findLatestMetricsByComponent(componentName);
        return metrics.stream()
                .filter(m -> metricName.equals(m.getMetricName()))
                .map(SystemMetrics::getMetricValue)
                .findFirst()
                .orElse(BigDecimal.ZERO);
    }

    private Double calculateAverageResponseTime(Instant startDate, Instant endDate) {
        List<Object[]> performanceData = systemMetricsRepository
                .findAverageMetricsByType("PERFORMANCE", startDate, endDate);

        return performanceData.stream()
                .filter(row -> "response_time".equals(row[1]))
                .map(row -> ((BigDecimal) row[2]).doubleValue())
                .findFirst()
                .orElse(0.0);
    }

    private BigDecimal calculateErrorRate(Instant startDate, Instant endDate) {
        // This would typically integrate with API usage logs
        return BigDecimal.valueOf(0.2); // Placeholder
    }

    private Long getActiveUsersCount() {
        // This would typically integrate with session data
        return 1247L; // Placeholder
    }

    private Long getDailyRequestsCount() {
        // This would typically integrate with API usage logs
        return 45890L; // Placeholder
    }

    private Integer getDatabaseConnectionsCount() {
        // This would typically integrate with connection pool metrics
        return 15; // Placeholder
    }

    private BigDecimal getCacheHitRate() {
        // This would typically integrate with cache metrics
        return BigDecimal.valueOf(94.3); // Placeholder
    }

    private Long getApiCallsToday() {
        // This would typically integrate with API usage logs
        return 12456L; // Placeholder
    }
}