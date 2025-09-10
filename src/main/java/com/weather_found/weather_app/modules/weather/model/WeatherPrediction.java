package com.weather_found.weather_app.modules.weather.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Weather predictions entity for storing ML-based weather forecasts
 */
@Entity
@Table(name = "weather_predictions", indexes = {
        @Index(name = "idx_prediction_location_time", columnList = "location_id, predicted_for"),
        @Index(name = "idx_prediction_event", columnList = "event_id"),
        @Index(name = "idx_prediction_date", columnList = "prediction_date"),
        @Index(name = "idx_prediction_model", columnList = "model_version")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherPrediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_prediction_location"))
    private Location location;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "prediction_date", nullable = false)
    private LocalDateTime predictionDate;

    @Column(name = "predicted_for", nullable = false)
    private LocalDateTime predictedFor;

    @Column(name = "temperature_min", precision = 5, scale = 2)
    private BigDecimal temperatureMin;

    @Column(name = "temperature_max", precision = 5, scale = 2)
    private BigDecimal temperatureMax;

    @Column(name = "precipitation_probability", precision = 5, scale = 2)
    private BigDecimal precipitationProbability;

    @Column(name = "precipitation_amount", precision = 5, scale = 2)
    private BigDecimal precipitationAmount;

    @Column(name = "weather_condition", length = 50)
    private String weatherCondition;

    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;

    @Column(name = "model_version", length = 20)
    private String modelVersion;

    @Column(name = "prediction_accuracy", precision = 5, scale = 2)
    private BigDecimal predictionAccuracy;

    @Column(name = "wind_speed", precision = 5, scale = 2)
    private BigDecimal windSpeed;

    @Column(name = "wind_direction")
    private Integer windDirection;

    @Column(precision = 5, scale = 2)
    private BigDecimal humidity;

    @Column(name = "cloud_cover")
    private Integer cloudCover;

    @Column(name = "uv_index", precision = 3, scale = 1)
    private BigDecimal uvIndex;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructor for basic prediction
    public WeatherPrediction(Location location, LocalDateTime predictedFor, BigDecimal temperatureMin,
            BigDecimal temperatureMax, BigDecimal precipitationProbability,
            String weatherCondition, BigDecimal confidenceScore, String modelVersion) {
        this.location = location;
        this.predictionDate = LocalDateTime.now();
        this.predictedFor = predictedFor;
        this.temperatureMin = temperatureMin;
        this.temperatureMax = temperatureMax;
        this.precipitationProbability = precipitationProbability;
        this.weatherCondition = weatherCondition;
        this.confidenceScore = confidenceScore;
        this.modelVersion = modelVersion;
    }

    // Constructor for event-specific prediction
    public WeatherPrediction(Location location, Long eventId, LocalDateTime predictedFor,
            BigDecimal temperatureMin, BigDecimal temperatureMax,
            BigDecimal precipitationProbability, String weatherCondition,
            BigDecimal confidenceScore, String modelVersion) {
        this(location, predictedFor, temperatureMin, temperatureMax, precipitationProbability,
                weatherCondition, confidenceScore, modelVersion);
        this.eventId = eventId;
    }
}
