package com.weather_found.weather_app.modules.weather.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Weather alerts entity for storing weather warnings and notifications
 */
@Entity
@Table(name = "weather_alerts", indexes = {
        @Index(name = "idx_alert_location_time", columnList = "location_id, alert_time"),
        @Index(name = "idx_alert_severity", columnList = "severity"),
        @Index(name = "idx_alert_type", columnList = "alert_type"),
        @Index(name = "idx_alert_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_alert_location"))
    private Location location;

    @Column(name = "alert_type", nullable = false, length = 50)
    private String alertType;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 20)
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL

    @Column(name = "alert_time", nullable = false)
    private LocalDateTime alertTime;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(length = 20, nullable = false)
    private String status; // ACTIVE, EXPIRED, CANCELLED

    @Column(name = "data_source", length = 50)
    private String dataSource;

    @Column(name = "external_alert_id", length = 100)
    private String externalAlertId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructor for creating new alerts
    public WeatherAlert(Location location, String alertType, String title, String description,
            String severity, LocalDateTime alertTime, LocalDateTime expiresAt, String dataSource) {
        this.location = location;
        this.alertType = alertType;
        this.title = title;
        this.description = description;
        this.severity = severity;
        this.alertTime = alertTime;
        this.expiresAt = expiresAt;
        this.dataSource = dataSource;
        this.status = "ACTIVE";
    }
}
