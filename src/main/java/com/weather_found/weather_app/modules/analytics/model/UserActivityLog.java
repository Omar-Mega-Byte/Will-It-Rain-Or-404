package com.weather_found.weather_app.modules.analytics.model;

import com.weather_found.weather_app.modules.shared.Base.BaseEntity;
import com.weather_found.weather_app.modules.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * Entity to track user behavior and activity for analytics
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Table(name = "user_activity_logs", indexes = {
        @Index(name = "idx_user_activity_user", columnList = "user_id"),
        @Index(name = "idx_user_activity_action", columnList = "action"),
        @Index(name = "idx_user_activity_timestamp", columnList = "timestamp"),
        @Index(name = "idx_user_activity_entity", columnList = "entity_type, entity_id")
})
public class UserActivityLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "action", nullable = false, length = 100)
    private String action;

    @Column(name = "entity_type", length = 50)
    private String entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "session_id", length = 100)
    private String sessionId;

    @Column(name = "details", columnDefinition = "JSON")
    private String details;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "response_time_ms")
    private Long responseTimeMs;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (timestamp == null) {
            timestamp = Instant.now();
        }
    }
}