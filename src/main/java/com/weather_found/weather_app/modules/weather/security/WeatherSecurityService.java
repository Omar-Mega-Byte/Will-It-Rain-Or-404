package com.weather_found.weather_app.modules.weather.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

/**
 * Security service for weather module - handles rate limiting and access
 * control
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherSecurityService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Rate limiting constants
    private static final int DEFAULT_REQUESTS_PER_MINUTE = 60;
    private static final int DASHBOARD_REQUESTS_PER_MINUTE = 30;
    private static final int API_REQUESTS_PER_MINUTE = 100;
    private static final int SEARCH_REQUESTS_PER_MINUTE = 20;

    // Security prefixes
    private static final String RATE_LIMIT_PREFIX = "rate_limit:";
    private static final String SUSPICIOUS_ACTIVITY_PREFIX = "suspicious:";
    private static final String BLOCKED_IP_PREFIX = "blocked_ip:";

    /**
     * Rate limiting result
     */
    public static class RateLimitResult {
        private final boolean allowed;
        private final long remainingRequests;
        private final long resetTimeSeconds;
        private final String message;

        public RateLimitResult(boolean allowed, long remainingRequests, long resetTimeSeconds, String message) {
            this.allowed = allowed;
            this.remainingRequests = remainingRequests;
            this.resetTimeSeconds = resetTimeSeconds;
            this.message = message;
        }

        public boolean isAllowed() {
            return allowed;
        }

        public long getRemainingRequests() {
            return remainingRequests;
        }

        public long getResetTimeSeconds() {
            return resetTimeSeconds;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * Check rate limit for general weather API requests
     */
    public RateLimitResult checkRateLimit(String clientId, String endpoint) {
        return checkRateLimit(clientId, endpoint, DEFAULT_REQUESTS_PER_MINUTE);
    }

    /**
     * Check rate limit for dashboard requests
     */
    public RateLimitResult checkDashboardRateLimit(String clientId) {
        return checkRateLimit(clientId, "dashboard", DASHBOARD_REQUESTS_PER_MINUTE);
    }

    /**
     * Check rate limit for external API requests
     */
    public RateLimitResult checkApiRateLimit(String clientId) {
        return checkRateLimit(clientId, "api", API_REQUESTS_PER_MINUTE);
    }

    /**
     * Check rate limit for search requests
     */
    public RateLimitResult checkSearchRateLimit(String clientId) {
        return checkRateLimit(clientId, "search", SEARCH_REQUESTS_PER_MINUTE);
    }

    /**
     * Generic rate limiting method
     */
    public RateLimitResult checkRateLimit(String clientId, String endpoint, int maxRequests) {
        if (isBlocked(clientId)) {
            return new RateLimitResult(false, 0, 0, "Client is temporarily blocked");
        }

        String key = RATE_LIMIT_PREFIX + endpoint + ":" + clientId + ":" + getCurrentMinute();

        try {
            Long currentCount = redisTemplate.opsForValue().increment(key);

            if (currentCount != null && currentCount == 1) {
                // First request in this minute, set expiration
                redisTemplate.expire(key, 1, TimeUnit.MINUTES);
            }

            long count = currentCount != null ? currentCount : 1;
            long remaining = Math.max(0, maxRequests - count);

            if (count > maxRequests) {
                // Rate limit exceeded
                logSuspiciousActivity(clientId, endpoint, count);
                return new RateLimitResult(false, 0, 60, "Rate limit exceeded");
            }

            return new RateLimitResult(true, remaining, 60, "Request allowed");

        } catch (Exception e) {
            log.error("Error checking rate limit for client: {} endpoint: {}", clientId, endpoint, e);
            // Fail open - allow the request if Redis is down
            return new RateLimitResult(true, maxRequests, 60, "Rate limiting unavailable");
        }
    }

    /**
     * Check if client is blocked
     */
    public boolean isBlocked(String clientId) {
        try {
            String blockKey = BLOCKED_IP_PREFIX + clientId;
            return redisTemplate.hasKey(blockKey);
        } catch (Exception e) {
            log.error("Error checking if client is blocked: {}", clientId, e);
            return false;
        }
    }

    /**
     * Block a client temporarily
     */
    public void blockClient(String clientId, int durationMinutes, String reason) {
        try {
            String blockKey = BLOCKED_IP_PREFIX + clientId;
            redisTemplate.opsForValue().set(blockKey, reason, durationMinutes, TimeUnit.MINUTES);
            log.warn("Blocked client {} for {} minutes. Reason: {}", clientId, durationMinutes, reason);
        } catch (Exception e) {
            log.error("Error blocking client: {}", clientId, e);
        }
    }

    /**
     * Unblock a client
     */
    public void unblockClient(String clientId) {
        try {
            String blockKey = BLOCKED_IP_PREFIX + clientId;
            redisTemplate.delete(blockKey);
            log.info("Unblocked client: {}", clientId);
        } catch (Exception e) {
            log.error("Error unblocking client: {}", clientId, e);
        }
    }

    /**
     * Check if user has access to admin endpoints
     */
    public boolean hasAdminAccess(String userRole) {
        return "ADMIN".equals(userRole) || "SUPER_ADMIN".equals(userRole);
    }

    /**
     * Check if user has access to analytics data
     */
    public boolean hasAnalyticsAccess(String userRole) {
        return hasAdminAccess(userRole) || "ANALYST".equals(userRole);
    }

    /**
     * Check if user can create weather alerts
     */
    public boolean canCreateAlerts(String userRole) {
        return hasAdminAccess(userRole) || "WEATHER_MANAGER".equals(userRole);
    }

    /**
     * Validate API key format and existence
     */
    public boolean isValidApiKey(String apiKey) {
        if (apiKey == null || apiKey.trim().isEmpty()) {
            return false;
        }

        // Basic validation - in production, this would check against a database
        return apiKey.length() >= 32 && apiKey.matches("^[a-zA-Z0-9]+$");
    }

    /**
     * Log suspicious activity
     */
    public void logSuspiciousActivity(String clientId, String activity, Object details) {
        try {
            String key = SUSPICIOUS_ACTIVITY_PREFIX + clientId;
            String logEntry = String.format("%s - %s: %s",
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                    activity,
                    details.toString());

            redisTemplate.opsForList().leftPush(key, logEntry);
            redisTemplate.expire(key, 24, TimeUnit.HOURS);

            // Check if client should be temporarily blocked
            Long activityCount = redisTemplate.opsForList().size(key);
            if (activityCount != null && activityCount > 10) {
                blockClient(clientId, 30, "Multiple rate limit violations");
            }

        } catch (Exception e) {
            log.error("Error logging suspicious activity for client: {}", clientId, e);
        }
    }

    /**
     * Get security metrics
     */
    public SecurityMetrics getSecurityMetrics() {
        try {
            // Count blocked IPs
            long blockedIps = countKeysWithPrefix(BLOCKED_IP_PREFIX);

            // Count suspicious activities (approximate)
            long suspiciousActivities = countKeysWithPrefix(SUSPICIOUS_ACTIVITY_PREFIX);

            return new SecurityMetrics(blockedIps, suspiciousActivities, LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error getting security metrics", e);
            return new SecurityMetrics(0, 0, LocalDateTime.now());
        }
    }

    /**
     * Validate request origin
     */
    public boolean isValidOrigin(String origin, String userAgent) {
        if (origin == null && userAgent == null) {
            return false;
        }

        // Basic bot detection
        if (userAgent != null) {
            String ua = userAgent.toLowerCase();
            if (ua.contains("bot") || ua.contains("crawler") || ua.contains("spider")) {
                return false;
            }
        }

        // In production, you would have a whitelist of allowed origins
        return true;
    }

    /**
     * Generate secure client identifier
     */
    public String generateClientId(String ipAddress, String userAgent) {
        if (ipAddress == null) {
            ipAddress = "unknown";
        }

        // Create a hash-based identifier that doesn't expose sensitive data
        int hash = (ipAddress + (userAgent != null ? userAgent : "")).hashCode();
        return "client_" + Math.abs(hash);
    }

    /**
     * Check if request size is within limits
     */
    public boolean isValidRequestSize(long contentLength) {
        // Limit request size to 1MB
        return contentLength <= 1024 * 1024;
    }

    /**
     * Sanitize input for security
     */
    public String sanitizeForSecurity(String input) {
        if (input == null) {
            return null;
        }

        return input.trim()
                .replaceAll("[<>\"'&;]", "") // Remove script injection characters
                .replaceAll("(?i)(script|javascript|vbscript)", "") // Remove script keywords
                .substring(0, Math.min(input.length(), 1000)); // Limit length
    }

    // Helper methods
    private String getCurrentMinute() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm"));
    }

    private long countKeysWithPrefix(String prefix) {
        try {
            return redisTemplate.keys(prefix + "*").size();
        } catch (Exception e) {
            log.debug("Error counting keys with prefix: {}", prefix);
            return 0;
        }
    }

    /**
     * Security metrics data class
     */
    public static class SecurityMetrics {
        private final long blockedIps;
        private final long suspiciousActivities;
        private final LocalDateTime timestamp;

        public SecurityMetrics(long blockedIps, long suspiciousActivities, LocalDateTime timestamp) {
            this.blockedIps = blockedIps;
            this.suspiciousActivities = suspiciousActivities;
            this.timestamp = timestamp;
        }

        public long getBlockedIps() {
            return blockedIps;
        }

        public long getSuspiciousActivities() {
            return suspiciousActivities;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }
    }
}
