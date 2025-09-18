package com.weather_found.weather_app.modules.analytics.dto.request;

import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.Instant;
import java.util.List;

/**
 * DTO for analytics report generation request
 */
@Data
@Builder
public class AnalyticsReportRequestDto {

    @NotNull
    private Instant startDate;

    @NotNull
    private Instant endDate;

    @Pattern(regexp = "USER|PREDICTION|SYSTEM|ENGAGEMENT|API|ALL", message = "Report type must be one of: USER, PREDICTION, SYSTEM, ENGAGEMENT, API, ALL")
    private String reportType;

    @Pattern(regexp = "CSV|JSON|PDF|EXCEL", message = "Format must be one of: CSV, JSON, PDF, EXCEL")
    private String format;

    private List<String> includeMetrics;
    private List<String> excludeMetrics;
    private String groupBy;
    private FilterCriteria filters;
    private boolean includeCharts;
    private boolean includeRawData;

    @Data
    @Builder
    public static class FilterCriteria {
        private List<String> userIds;
        private List<String> locations;
        private List<String> predictionTypes;
        private List<String> apiEndpoints;
        private String minAccuracy;
        private String maxResponseTime;
    }
}