package com.weather_found.weather_app.modules.analytics.model;

import com.weather_found.weather_app.modules.shared.Base.BaseEntity;
import com.weather_found.weather_app.modules.weather.model.Location;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity to track prediction accuracy for analytics and model improvement
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Table(name = "prediction_accuracy_logs", indexes = {
        @Index(name = "idx_prediction_location", columnList = "location_id"),
        @Index(name = "idx_prediction_timestamp", columnList = "prediction_date"),
        @Index(name = "idx_prediction_model", columnList = "model_version"),
        @Index(name = "idx_prediction_accuracy", columnList = "accuracy_score")
})
public class PredictionAccuracyLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Column(name = "prediction_date", nullable = false)
    private Instant predictionDate;

    @Column(name = "predicted_for", nullable = false)
    private Instant predictedFor;

    @Column(name = "model_version", nullable = false, length = 50)
    private String modelVersion;

    @Column(name = "prediction_type", nullable = false, length = 50)
    private String predictionType;

    @Column(name = "predicted_value", precision = 10, scale = 4)
    private BigDecimal predictedValue;

    @Column(name = "actual_value", precision = 10, scale = 4)
    private BigDecimal actualValue;

    @Column(name = "accuracy_score", precision = 5, scale = 2)
    private BigDecimal accuracyScore;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "error_margin", precision = 8, scale = 4)
    private BigDecimal errorMargin;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @Column(name = "is_verified", nullable = false)
    private Boolean isVerified = false;

    @Column(name = "verification_date")
    private Instant verificationDate;

    @Column(name = "metadata", columnDefinition = "JSON")
    private String metadata;
}