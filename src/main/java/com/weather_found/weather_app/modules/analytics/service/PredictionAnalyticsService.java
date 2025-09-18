package com.weather_found.weather_app.modules.analytics.service;

import com.weather_found.weather_app.modules.analytics.dto.response.PredictionMetricsResponseDto;
import com.weather_found.weather_app.modules.analytics.model.PredictionAccuracyLog;
import com.weather_found.weather_app.modules.analytics.repository.PredictionAccuracyLogRepository;
import com.weather_found.weather_app.modules.weather.model.Location;
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
 * Service for tracking and analyzing prediction accuracy
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PredictionAnalyticsService {

    private final PredictionAccuracyLogRepository accuracyLogRepository;

    /**
     * Record prediction accuracy for analytics
     */
    @Transactional
    public void recordPredictionAccuracy(Location location, Instant predictionDate, Instant predictedFor,
            String modelVersion, String predictionType, BigDecimal predictedValue,
            BigDecimal actualValue, BigDecimal confidenceScore, Long responseTimeMs,
            String metadata) {
        try {
            // Calculate accuracy score
            BigDecimal accuracyScore = calculateAccuracyScore(predictedValue, actualValue, predictionType);
            BigDecimal errorMargin = actualValue.subtract(predictedValue).abs();

            PredictionAccuracyLog accuracyLog = new PredictionAccuracyLog();
            accuracyLog.setLocation(location);
            accuracyLog.setPredictionDate(predictionDate);
            accuracyLog.setPredictedFor(predictedFor);
            accuracyLog.setModelVersion(modelVersion);
            accuracyLog.setPredictionType(predictionType);
            accuracyLog.setPredictedValue(predictedValue);
            accuracyLog.setActualValue(actualValue);
            accuracyLog.setAccuracyScore(accuracyScore);
            accuracyLog.setConfidenceScore(confidenceScore);
            accuracyLog.setErrorMargin(errorMargin);
            accuracyLog.setResponseTimeMs(responseTimeMs);
            accuracyLog.setIsVerified(true);
            accuracyLog.setVerificationDate(Instant.now());
            accuracyLog.setMetadata(metadata);

            accuracyLogRepository.save(accuracyLog);
            log.debug("Recorded prediction accuracy: {} for type: {}", accuracyScore, predictionType);
        } catch (Exception e) {
            log.error("Failed to record prediction accuracy: {}", e.getMessage(), e);
        }
    }

    /**
     * Get comprehensive prediction metrics
     */
    public PredictionMetricsResponseDto getPredictionMetrics(int daysBack) {
        Instant endDate = Instant.now();
        Instant startDate = endDate.minus(daysBack, ChronoUnit.DAYS);

        // Get basic statistics
        List<PredictionAccuracyLog> verifiedPredictions = accuracyLogRepository
                .findByIsVerifiedTrueAndPredictionDateBetween(startDate, endDate);

        Long totalPredictions = (long) verifiedPredictions.size();
        Long verifiedCount = totalPredictions;

        BigDecimal overallAccuracy = verifiedPredictions.stream()
                .map(PredictionAccuracyLog::getAccuracyScore)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, totalPredictions)), 2, RoundingMode.HALF_UP);

        Double averageResponseTime = verifiedPredictions.stream()
                .filter(p -> p.getResponseTimeMs() != null)
                .mapToLong(PredictionAccuracyLog::getResponseTimeMs)
                .average()
                .orElse(0.0);

        // Get model performance
        List<Object[]> modelData = accuracyLogRepository.findAccuracyByModelVersion(startDate, endDate);
        List<PredictionMetricsResponseDto.ModelPerformance> modelPerformances = modelData.stream()
                .map(row -> PredictionMetricsResponseDto.ModelPerformance.builder()
                        .modelVersion((String) row[0])
                        .accuracy((BigDecimal) row[1])
                        .predictionCount(((Number) row[2]).longValue())
                        .errorMargin(calculateAverageErrorMargin((String) row[0], startDate, endDate))
                        .build())
                .collect(Collectors.toList());

        // Get accuracy trends
        List<Object[]> trendsData = accuracyLogRepository.findAccuracyTrends(startDate, endDate);
        List<PredictionMetricsResponseDto.AccuracyTrend> accuracyTrends = trendsData.stream()
                .map(row -> PredictionMetricsResponseDto.AccuracyTrend.builder()
                        .date(row[0].toString())
                        .accuracy((BigDecimal) row[1])
                        .build())
                .collect(Collectors.toList());

        // Get prediction type metrics
        List<Object[]> typeData = accuracyLogRepository.findAccuracyByPredictionType(startDate, endDate);
        Map<String, PredictionMetricsResponseDto.PredictionTypeMetrics> predictionTypeMetrics = typeData.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> PredictionMetricsResponseDto.PredictionTypeMetrics.builder()
                                .accuracy((BigDecimal) row[1])
                                .totalCount(((Number) row[2]).longValue())
                                .errorMargin(calculateTypeErrorMargin((String) row[0], startDate, endDate))
                                .averageResponseTime(calculateTypeResponseTime((String) row[0], startDate, endDate))
                                .build()));

        // Get location accuracies
        List<Object[]> locationData = accuracyLogRepository.findLocationAccuracyStatistics(startDate, endDate);
        List<PredictionMetricsResponseDto.LocationAccuracy> locationAccuracies = locationData.stream()
                .map(row -> PredictionMetricsResponseDto.LocationAccuracy.builder()
                        .locationName((String) row[0])
                        .accuracy((BigDecimal) row[1])
                        .predictionCount(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());

        // Get confidence analysis
        PredictionMetricsResponseDto.ConfidenceAnalysis confidenceAnalysis = buildConfidenceAnalysis(startDate,
                endDate);

        return PredictionMetricsResponseDto.builder()
                .totalPredictions(totalPredictions)
                .overallAccuracyRate(overallAccuracy)
                .averageResponseTimeMs(averageResponseTime)
                .verifiedPredictions(verifiedCount)
                .modelPerformances(modelPerformances)
                .accuracyTrends(accuracyTrends)
                .predictionTypeMetrics(predictionTypeMetrics)
                .locationAccuracies(locationAccuracies)
                .confidenceAnalysis(confidenceAnalysis)
                .build();
    }

    /**
     * Calculate accuracy score based on prediction type
     */
    private BigDecimal calculateAccuracyScore(BigDecimal predicted, BigDecimal actual, String predictionType) {
        if (predicted == null || actual == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal difference = predicted.subtract(actual).abs();
        BigDecimal tolerance = getToleranceForType(predictionType);

        if (difference.compareTo(tolerance) <= 0) {
            // Perfect or within tolerance
            BigDecimal score = BigDecimal.valueOf(100)
                    .subtract(difference.multiply(BigDecimal.valueOf(50)).divide(tolerance, 2, RoundingMode.HALF_UP));
            return score.max(BigDecimal.valueOf(50)); // Minimum 50% if within tolerance
        } else {
            // Outside tolerance - calculate degraded accuracy
            BigDecimal maxDifference = tolerance.multiply(BigDecimal.valueOf(2));
            BigDecimal score = BigDecimal.valueOf(50)
                    .subtract(difference.subtract(tolerance).multiply(BigDecimal.valueOf(50)).divide(maxDifference, 2,
                            RoundingMode.HALF_UP));
            return score.max(BigDecimal.ZERO);
        }
    }

    private BigDecimal getToleranceForType(String predictionType) {
        return switch (predictionType.toLowerCase()) {
            case "temperature" -> BigDecimal.valueOf(2.0); // ±2°C
            case "humidity" -> BigDecimal.valueOf(5.0); // ±5%
            case "pressure" -> BigDecimal.valueOf(2.0); // ±2 hPa
            case "wind_speed" -> BigDecimal.valueOf(3.0); // ±3 km/h
            case "precipitation" -> BigDecimal.valueOf(1.0); // ±1 mm
            default -> BigDecimal.valueOf(1.0);
        };
    }

    private BigDecimal calculateAverageErrorMargin(String modelVersion, Instant startDate, Instant endDate) {
        List<PredictionAccuracyLog> logs = accuracyLogRepository
                .findByIsVerifiedTrueAndPredictionDateBetween(startDate, endDate)
                .stream()
                .filter(log -> modelVersion.equals(log.getModelVersion()))
                .collect(Collectors.toList());

        return logs.stream()
                .map(PredictionAccuracyLog::getErrorMargin)
                .filter(margin -> margin != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, logs.size())), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTypeErrorMargin(String predictionType, Instant startDate, Instant endDate) {
        List<PredictionAccuracyLog> logs = accuracyLogRepository
                .findByIsVerifiedTrueAndPredictionDateBetween(startDate, endDate)
                .stream()
                .filter(log -> predictionType.equals(log.getPredictionType()))
                .collect(Collectors.toList());

        return logs.stream()
                .map(PredictionAccuracyLog::getErrorMargin)
                .filter(margin -> margin != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, logs.size())), 2, RoundingMode.HALF_UP);
    }

    private Double calculateTypeResponseTime(String predictionType, Instant startDate, Instant endDate) {
        return accuracyLogRepository
                .findByIsVerifiedTrueAndPredictionDateBetween(startDate, endDate)
                .stream()
                .filter(log -> predictionType.equals(log.getPredictionType()) && log.getResponseTimeMs() != null)
                .mapToLong(PredictionAccuracyLog::getResponseTimeMs)
                .average()
                .orElse(0.0);
    }

    private PredictionMetricsResponseDto.ConfidenceAnalysis buildConfidenceAnalysis(Instant startDate,
            Instant endDate) {
        List<Object[]> confidenceData = accuracyLogRepository.findConfidenceAccuracyCorrelation(startDate, endDate);

        List<PredictionMetricsResponseDto.ConfidenceBand> confidenceBands = confidenceData.stream()
                .map(row -> PredictionMetricsResponseDto.ConfidenceBand.builder()
                        .confidenceRange((BigDecimal) row[0])
                        .averageAccuracy((BigDecimal) row[1])
                        .count(1L) // This would need to be calculated properly
                        .build())
                .collect(Collectors.toList());

        BigDecimal averageConfidence = confidenceBands.stream()
                .map(PredictionMetricsResponseDto.ConfidenceBand::getConfidenceRange)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(Math.max(1, confidenceBands.size())), 2, RoundingMode.HALF_UP);

        return PredictionMetricsResponseDto.ConfidenceAnalysis.builder()
                .averageConfidence(averageConfidence)
                .confidenceAccuracyCorrelation(BigDecimal.valueOf(0.85)) // This would be calculated
                .confidenceBands(confidenceBands)
                .build();
    }
}