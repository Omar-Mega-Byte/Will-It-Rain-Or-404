package com.weather_found.weather_app.modules.analytics.service;

import com.weather_found.weather_app.modules.analytics.model.ApiUsageLog;
import com.weather_found.weather_app.modules.analytics.repository.ApiUsageLogRepository;
import com.weather_found.weather_app.modules.user.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Service for tracking API usage analytics
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ApiUsageAnalyticsService {

    private final ApiUsageLogRepository apiUsageLogRepository;

    /**
     * Record API usage for analytics
     */
    @Transactional
    public void recordApiUsage(String endpoint, String httpMethod, User user, Integer responseStatus,
            Long responseTimeMs, Long requestSizeBytes, Long responseSizeBytes,
            String ipAddress, String userAgent, String sessionId,
            String errorMessage, String requestParameters) {
        try {
            ApiUsageLog usageLog = new ApiUsageLog();
            usageLog.setEndpoint(endpoint);
            usageLog.setHttpMethod(httpMethod);
            usageLog.setUser(user);
            usageLog.setResponseStatus(responseStatus);
            usageLog.setResponseTimeMs(responseTimeMs);
            usageLog.setRequestSizeBytes(requestSizeBytes);
            usageLog.setResponseSizeBytes(responseSizeBytes);
            usageLog.setIpAddress(ipAddress);
            usageLog.setUserAgent(userAgent);
            usageLog.setSessionId(sessionId);
            usageLog.setErrorMessage(errorMessage);
            usageLog.setRequestParameters(requestParameters);

            apiUsageLogRepository.save(usageLog);
            log.debug("Recorded API usage: {} {} - {}", httpMethod, endpoint, responseStatus);
        } catch (Exception e) {
            log.error("Failed to record API usage: {}", e.getMessage(), e);
        }
    }

    /**
     * Check if user has exceeded rate limit
     */
    public boolean hasExceededRateLimit(User user, int requestsPerMinute) {
        if (user == null)
            return false;

        Instant oneMinuteAgo = Instant.now().minusSeconds(60);
        Long recentRequests = apiUsageLogRepository.countApiCallsByUser(user, oneMinuteAgo, Instant.now());

        return recentRequests > requestsPerMinute;
    }

    /**
     * Get user's API usage statistics
     */
    public Long getUserApiUsageCount(User user, Instant startDate, Instant endDate) {
        return apiUsageLogRepository.countApiCallsByUser(user, startDate, endDate);
    }
}