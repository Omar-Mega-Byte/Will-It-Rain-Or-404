package com.weather_found.weather_app.modules.location.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * Simple Location entity for the location module
 */
@Entity
@Table(name = "locations", indexes = {
        @Index(name = "idx_location_coords", columnList = "latitude, longitude"),
        @Index(name = "idx_location_name", columnList = "name"),
        @Index(name = "idx_location_country_city", columnList = "country, city")
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
    @Column(name = "begin_date", nullable = false)
    private LocalDate beginDate;

    @Column(name = "end_date")
    private LocalDate endDate;

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

    // Constructor for commonly used fields with dates
    public Location(String name, BigDecimal latitude, BigDecimal longitude, String country, String timezone,
            LocalDate beginDate, LocalDate endDate) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.timezone = timezone;
        this.beginDate = beginDate;
        this.endDate = endDate;
    }

    // Constructor for basic location with required beginDate
    public Location(String name, BigDecimal latitude, BigDecimal longitude, LocalDate beginDate) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.beginDate = beginDate;
    }

    // Constructor for commonly used fields
    public Location(String name, BigDecimal latitude, BigDecimal longitude, String country, String timezone) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.country = country;
        this.timezone = timezone;
    }

    // Constructor for basic location
    public Location(String name, BigDecimal latitude, BigDecimal longitude) {
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}