package com.weather_found.weather_app.modules.weather.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Weather data entity for storing actual weather observations
 */
@Entity
@Table(name = "weather_data", indexes = {
        @Index(name = "idx_weather_location_time", columnList = "location_id, recorded_at"),
        @Index(name = "idx_weather_recorded_at", columnList = "recorded_at"),
        @Index(name = "idx_weather_data_source", columnList = "data_source")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherDataEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_weather_data_location"))
    private Location location;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    @Column(precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(precision = 5, scale = 2)
    private BigDecimal humidity;

    @Column(precision = 7, scale = 2)
    private BigDecimal pressure;

    @Column(name = "wind_speed", precision = 5, scale = 2)
    private BigDecimal windSpeed;

    @Column(name = "wind_direction")
    private Integer windDirection;

    @Column(precision = 5, scale = 2)
    private BigDecimal precipitation;

    @Column(precision = 5, scale = 2)
    private BigDecimal visibility;

    @Column(name = "weather_condition", length = 50)
    private String weatherCondition;

    @Column(name = "cloud_cover")
    private Integer cloudCover;

    @Column(name = "uv_index", precision = 3, scale = 1)
    private BigDecimal uvIndex;

    @Column(name = "data_source", length = 50)
    private String dataSource;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructor for essential weather data
    public WeatherDataEntity(Location location, LocalDateTime recordedAt, BigDecimal temperature,
            BigDecimal humidity, String weatherCondition, String dataSource) {
        this.location = location;
        this.recordedAt = recordedAt;
        this.temperature = temperature;
        this.humidity = humidity;
        this.weatherCondition = weatherCondition;
        this.dataSource = dataSource;
    }
}
