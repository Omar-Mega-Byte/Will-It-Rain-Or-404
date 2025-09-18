package com.weather_found.weather_app.modules.analytics.listener;

import com.weather_found.weather_app.modules.analytics.event.ApiUsageEvent;
import com.weather_found.weather_app.modules.analytics.event.PredictionAccuracyEvent;
import com.weather_found.weather_app.modules.analytics.event.UserActivityEvent;
import com.weather_found.weather_app.modules.analytics.service.ApiUsageAnalyticsService;
import com.weather_found.weather_app.modules.analytics.service.PredictionAnalyticsService;
import com.weather_found.weather_app.modules.analytics.service.UserAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Event listener for analytics events to decouple analytics tracking from
 * business logic
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.analytics.enabled", havingValue = "true", matchIfMissing = true)
public class AnalyticsEventListener {

    private final UserAnalyticsService userAnalyticsService;
    private final PredictionAnalyticsService predictionAnalyticsService;
    private final ApiUsageAnalyticsService apiUsageAnalyticsService;

    /**
     * Handle user activity events asynchronously
     */
    @Async
    @EventListener
    public void handleUserActivityEvent(UserActivityEvent event) {
        try {
            log.debug("Processing user activity event: {} for user: {}",
                    event.getAction(), event.getUser().getUsername());

            userAnalyticsService.recordActivity(
                    event.getUser(),
                    event.getAction(),
                    event.getEntityType(),
                    event.getEntityId(),
                    event.getIpAddress(),
                    event.getUserAgent(),
                    event.getSessionId(),
                    event.getDetails(),
                    event.getResponseTimeMs());
        } catch (Exception e) {
            log.error("Failed to process user activity event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle prediction accuracy events asynchronously
     */
    @Async
    @EventListener
    public void handlePredictionAccuracyEvent(PredictionAccuracyEvent event) {
        try {
            log.debug("Processing prediction accuracy event: {} for location: {}",
                    event.getPredictionType(), event.getLocation().getName());

            predictionAnalyticsService.recordPredictionAccuracy(
                    event.getLocation(),
                    event.getPredictionDate(),
                    event.getPredictedFor(),
                    event.getModelVersion(),
                    event.getPredictionType(),
                    event.getPredictedValue(),
                    event.getActualValue(),
                    event.getConfidenceScore(),
                    event.getResponseTimeMs(),
                    event.getMetadata());
        } catch (Exception e) {
            log.error("Failed to process prediction accuracy event: {}", e.getMessage(), e);
        }
    }

    /**
     * Handle API usage events asynchronously
     */
    @Async
    @EventListener
    public void handleApiUsageEvent(ApiUsageEvent event) {
        try {
            log.debug("Processing API usage event: {} {} - status: {}",
                    event.getHttpMethod(), event.getEndpoint(), event.getResponseStatus());

            apiUsageAnalyticsService.recordApiUsage(
                    event.getEndpoint(),
                    event.getHttpMethod(),
                    event.getUser(),
                    event.getResponseStatus(),
                    event.getResponseTimeMs(),
                    event.getRequestSizeBytes(),
                    event.getResponseSizeBytes(),
                    event.getIpAddress(),
                    event.getUserAgent(),
                    event.getSessionId(),
                    event.getErrorMessage(),
                    event.getRequestParameters());
        } catch (Exception e) {
            log.error("Failed to process API usage event: {}", e.getMessage(), e);
        }
    }
}