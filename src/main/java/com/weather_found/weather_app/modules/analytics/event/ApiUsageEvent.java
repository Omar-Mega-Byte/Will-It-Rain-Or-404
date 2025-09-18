package com.weather_found.weather_app.modules.analytics.event;

import com.weather_found.weather_app.modules.user.model.User;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.Instant;

/**
 * Event published when an API call is made and should be tracked for analytics
 */
@Data
@AllArgsConstructor
public class ApiUsageEvent {
    private String endpoint;
    private String httpMethod;
    private User user;
    private Integer responseStatus;
    private Long responseTimeMs;
    private Long requestSizeBytes;
    private Long responseSizeBytes;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String errorMessage;
    private String requestParameters;
    private Instant timestamp;

    public ApiUsageEvent(String endpoint, String httpMethod, User user, Integer responseStatus, Long responseTimeMs) {
        this.endpoint = endpoint;
        this.httpMethod = httpMethod;
        this.user = user;
        this.responseStatus = responseStatus;
        this.responseTimeMs = responseTimeMs;
        this.timestamp = Instant.now();
    }
}