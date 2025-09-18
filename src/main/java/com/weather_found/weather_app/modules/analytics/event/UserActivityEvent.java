package com.weather_found.weather_app.modules.analytics.event;

import com.weather_found.weather_app.modules.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

/**
 * Event published when a user performs an action that should be tracked for
 * analytics
 */
@Data
@AllArgsConstructor
public class UserActivityEvent {
    private User user;
    private String action;
    private String entityType;
    private Long entityId;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String details;
    private Long responseTimeMs;
    private Instant timestamp;

    public UserActivityEvent(User user, String action, String entityType, Long entityId) {
        this.user = user;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.timestamp = Instant.now();
    }

    public UserActivityEvent(User user, String action) {
        this(user, action, null, null);
    }
}