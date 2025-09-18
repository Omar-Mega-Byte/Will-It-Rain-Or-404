package com.weather_found.weather_app.modules.analytics.model;

import com.weather_found.weather_app.modules.shared.Base.BaseEntity;
import com.weather_found.weather_app.modules.user.model.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;

/**
 * Entity to track user sessions for engagement analytics
 */
@Data
@Entity
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Table(name = "user_sessions", indexes = {
        @Index(name = "idx_user_sessions_user", columnList = "user_id"),
        @Index(name = "idx_user_sessions_start", columnList = "session_start"),
        @Index(name = "idx_user_sessions_end", columnList = "session_end"),
        @Index(name = "idx_user_sessions_id", columnList = "session_id")
})
public class UserSession extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "session_id", nullable = false, unique = true, length = 100)
    private String sessionId;

    @Column(name = "session_start", nullable = false)
    private Instant sessionStart;

    @Column(name = "session_end")
    private Instant sessionEnd;

    @Column(name = "duration_seconds")
    private Long durationSeconds;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "device_type", length = 50)
    private String deviceType;

    @Column(name = "browser", length = 100)
    private String browser;

    @Column(name = "operating_system", length = 100)
    private String operatingSystem;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "page_views")
    private Integer pageViews = 0;

    @Column(name = "actions_performed")
    private Integer actionsPerformed = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (sessionStart == null) {
            sessionStart = Instant.now();
        }
    }

    /**
     * Calculate and set session duration when session ends
     */
    public void endSession() {
        if (sessionEnd == null && sessionStart != null) {
            sessionEnd = Instant.now();
            durationSeconds = sessionEnd.getEpochSecond() - sessionStart.getEpochSecond();
            isActive = false;
        }
    }
}