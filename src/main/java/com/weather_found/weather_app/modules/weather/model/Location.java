package com.weather_found.weather_app.modules.weather.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Location entity for weather data
 */
@Entity(name = "WeatherLocation")
@Table(name = "weather_locations", indexes = {
        @Index(name = "idx_weather_location_coords", columnList = "latitude, longitude"),
        @Index(name = "idx_weather_location_name", columnList = "name")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(nullable = false, precision = 11, scale = 8)
    private BigDecimal longitude;

    @Column(length = 100)
    private String country;

    @Column(length = 100)
    private String state;

    @Column(length = 100)
    private String city;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 50)
    private String timezone;

    @Column
    private Integer elevation;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructor for commonly used fields
    public Location(String name, BigDecimal latitude, BigDecimal longitude, String country, String timezone) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.timezone = timezone;
    }
}
