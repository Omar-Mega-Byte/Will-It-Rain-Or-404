package com.weather_found.weather_app.modules.analytics.model;

import com.weather_found.weather_app.modules.shared.Base.BaseEntity;
import com.weather_found.weather_app.modules.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * Entity to track API usage for analytics and rate limiting
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Table(name = "api_usage_logs", indexes = {
        @Index(name = "idx_api_usage_user", columnList = "user_id"),
        @Index(name = "idx_api_usage_endpoint", columnList = "endpoint"),
        @Index(name = "idx_api_usage_timestamp", columnList = "timestamp"),
        @Index(name = "idx_api_usage_status", columnList = "response_status"),
        @Index(name = "idx_api_usage_method", columnList = "http_method")
})
public class ApiUsageLog extends BaseEntity {

    @Column(name = "endpoint", nullable = false, length = 255)
    private String endpoint;

    @Column(name = "http_method", nullable = false, length = 10)
    private String httpMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "response_status", nullable = false)
    private Integer responseStatus;

    @Column(name = "response_time_ms", nullable = false)
    private Long responseTimeMs;

    @Column(name = "request_size_bytes")
    private Long requestSizeBytes;

    @Column(name = "response_size_bytes")
    private Long responseSizeBytes;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "request_parameters", columnDefinition = "JSON")
    private String requestParameters;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}