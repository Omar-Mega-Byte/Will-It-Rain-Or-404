package com.weather_found.weather_app.modules.analytics.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * DTO for prediction accuracy metrics response
 */
@Data
@Builder
public class PredictionMetricsResponseDto {
    private Long totalPredictions;
    private BigDecimal overallAccuracyRate;
    private Double averageResponseTimeMs;
    private Long verifiedPredictions;
    private List<ModelPerformance> modelPerformances;
    private List<AccuracyTrend> accuracyTrends;
    private Map<String, PredictionTypeMetrics> predictionTypeMetrics;
    private List<LocationAccuracy> locationAccuracies;
    private ConfidenceAnalysis confidenceAnalysis;

    @Data
    @Builder
    public static class ModelPerformance {
        private String modelVersion;
        private BigDecimal accuracy;
        private Long predictionCount;
        private BigDecimal errorMargin;
    }

    @Data
    @Builder
    public static class AccuracyTrend {
        private String date;
        private BigDecimal accuracy;
    }

    @Data
    @Builder
    public static class PredictionTypeMetrics {
        private BigDecimal accuracy;
        private BigDecimal errorMargin;
        private Double averageResponseTime;
        private Long totalCount;
    }

    @Data
    @Builder
    public static class LocationAccuracy {
        private String locationName;
        private BigDecimal accuracy;
        private Long predictionCount;
    }

    @Data
    @Builder
    public static class ConfidenceAnalysis {
        private BigDecimal averageConfidence;
        private BigDecimal confidenceAccuracyCorrelation;
        private List<ConfidenceBand> confidenceBands;
    }

    @Data
    @Builder
    public static class ConfidenceBand {
        private BigDecimal confidenceRange;
        private BigDecimal averageAccuracy;
        private Long count;
    }
}