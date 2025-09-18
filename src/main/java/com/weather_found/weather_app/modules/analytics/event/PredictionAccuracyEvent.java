package com.weather_found.weather_app.modules.analytics.event;

import com.weather_found.weather_app.modules.weather.model.Location;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Event published when a prediction accuracy is verified and should be recorded
 * for analytics
 */
@Data
@AllArgsConstructor
public class PredictionAccuracyEvent {
    private Location location;
    private Instant predictionDate;
    private Instant predictedFor;
    private String modelVersion;
    private String predictionType;
    private BigDecimal predictedValue;
    private BigDecimal actualValue;
    private BigDecimal confidenceScore;
    private Long responseTimeMs;
    private String metadata;
    private Instant timestamp;

    public PredictionAccuracyEvent(Location location, Instant predictionDate, Instant predictedFor,
            String modelVersion, String predictionType, BigDecimal predictedValue,
            BigDecimal actualValue, BigDecimal confidenceScore) {
        this.location = location;
        this.predictionDate = predictionDate;
        this.predictedFor = predictedFor;
        this.modelVersion = modelVersion;
        this.predictionType = predictionType;
        this.predictedValue = predictedValue;
        this.actualValue = actualValue;
        this.confidenceScore = confidenceScore;
        this.timestamp = Instant.now();
    }
}