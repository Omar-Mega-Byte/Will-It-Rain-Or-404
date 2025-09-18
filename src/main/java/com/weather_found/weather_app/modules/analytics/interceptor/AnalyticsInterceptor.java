package com.weather_found.weather_app.modules.analytics.interceptor;

import com.weather_found.weather_app.modules.analytics.event.ApiUsageEvent;
import com.weather_found.weather_app.modules.analytics.service.UserAnalyticsService;
import com.weather_found.weather_app.modules.user.model.User;
import com.weather_found.weather_app.modules.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

/**
 * Interceptor to automatically track API usage and user activity for analytics
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.analytics.enabled", havingValue = "true", matchIfMissing = true)
public class AnalyticsInterceptor implements HandlerInterceptor {

    private final ApplicationEventPublisher eventPublisher;
    private final UserRepository userRepository;
    private final UserAnalyticsService userAnalyticsService;

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler) {
        // Record request start time for response time calculation
        request.setAttribute("startTime", System.currentTimeMillis());

        // Start or update user session for authenticated users
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String sessionId = request.getSession().getId();

            Optional<User> userOpt = userRepository.findByUsername(auth.getName());
            if (userOpt.isPresent()) {
                // Update session activity
                userAnalyticsService.updateSessionActivity(sessionId, true, false);
            }
        }

        return true;
    }

    @Override
    public void afterCompletion(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull Object handler, @Nullable Exception ex) {
        try {
            // Calculate response time
            Long startTime = (Long) request.getAttribute("startTime");
            long responseTime = startTime != null ? System.currentTimeMillis() - startTime : 0;

            // Get user information if authenticated
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User user = null;
            if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
                Optional<User> userOpt = userRepository.findByUsername(auth.getName());
                if (userOpt.isPresent()) {
                    user = userOpt.get();

                    // Update session activity for actions
                    if (isActionEndpoint(request.getRequestURI())) {
                        userAnalyticsService.updateSessionActivity(request.getSession().getId(), false, true);
                    }
                }
            }

            // Skip tracking for internal endpoints
            if (shouldTrackEndpoint(request.getRequestURI())) {
                // Publish API usage event
                ApiUsageEvent apiEvent = new ApiUsageEvent(
                        request.getRequestURI(),
                        request.getMethod(),
                        user,
                        response.getStatus(),
                        responseTime,
                        getRequestSize(request),
                        getResponseSize(response),
                        getClientIpAddress(request),
                        request.getHeader("User-Agent"),
                        request.getSession().getId(),
                        ex != null ? ex.getMessage() : null,
                        getRequestParameters(request),
                        java.time.Instant.now());

                eventPublisher.publishEvent(apiEvent);
            }

        } catch (Exception e) {
            log.error("Error in analytics interceptor: {}", e.getMessage());
        }
    }

    private boolean shouldTrackEndpoint(String uri) {
        // Don't track internal endpoints like health checks, static resources, etc.
        return !uri.startsWith("/actuator") &&
                !uri.startsWith("/error") &&
                !uri.startsWith("/favicon.ico") &&
                !uri.startsWith("/swagger") &&
                !uri.startsWith("/v3/api-docs");
    }

    private boolean isActionEndpoint(String uri) {
        // Consider POST, PUT, DELETE as actions
        return uri.contains("/api/") &&
                (uri.contains("create") || uri.contains("update") || uri.contains("delete") ||
                        uri.contains("search") || uri.contains("predict"));
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }

        return request.getRemoteAddr();
    }

    private Long getRequestSize(HttpServletRequest request) {
        String contentLength = request.getHeader("Content-Length");
        if (contentLength != null) {
            try {
                return Long.parseLong(contentLength);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Long getResponseSize(HttpServletResponse response) {
        // This would typically be set by a response wrapper
        // For now, returning null as it requires additional setup
        return null;
    }

    private String getRequestParameters(HttpServletRequest request) {
        // Simple parameter extraction - in production, be careful about sensitive data
        String queryString = request.getQueryString();
        if (queryString != null && !queryString.isEmpty()) {
            return queryString.length() > 500 ? queryString.substring(0, 500) + "..." : queryString;
        }
        return null;
    }
}