package com.weather_found.weather_app.modules.analytics.model;

import com.weather_found.weather_app.modules.shared.Base.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Entity to track system performance metrics for monitoring and analytics
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Table(name = "system_metrics", indexes = {
        @Index(name = "idx_system_metrics_timestamp", columnList = "timestamp"),
        @Index(name = "idx_system_metrics_type", columnList = "metric_type"),
        @Index(name = "idx_system_metrics_component", columnList = "component_name")
})
public class SystemMetrics extends BaseEntity {

    @Column(name = "metric_type", nullable = false, length = 50)
    private String metricType;

    @Column(name = "component_name", nullable = false, length = 100)
    private String componentName;

    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    @Column(name = "metric_value", nullable = false, precision = 15, scale = 4)
    private BigDecimal metricValue;

    @Column(name = "unit", length = 20)
    private String unit;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "tags", columnDefinition = "JSON")
    private String tags;

    @Column(name = "threshold_min", precision = 15, scale = 4)
    private BigDecimal thresholdMin;

    @Column(name = "threshold_max", precision = 15, scale = 4)
    private BigDecimal thresholdMax;

    @Column(name = "is_healthy", nullable = false)
    private Boolean isHealthy = true;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}