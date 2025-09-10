package com.weather_found.weather_app.modules.weather.aspect;

import com.weather_found.weather_app.modules.weather.service.WeatherAnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Aspect for automatically tracking weather API usage
 */
@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
public class WeatherTrackingAspect {

    private final WeatherAnalyticsService analyticsService;

    /**
     * Track successful API calls to cached weather endpoints
     */
    @AfterReturning("execution(* com.weather_found.weather_app.modules.weather.controller.CachedWeatherController.*(..))")
    public void trackSuccessfulApiCall(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String endpoint = "cached-weather/" + methodName;

            // Get location parameter if available
            String location = extractLocationParameter(joinPoint);

            // Get user ID from security context
            String userId = getCurrentUserId();

            // Track the API request
            analyticsService.trackApiRequest(endpoint, location, userId);

        } catch (Exception e) {
            log.error("Error tracking API call", e);
        }
    }

    /**
     * Track failed API calls
     */
    @AfterThrowing(pointcut = "execution(* com.weather_found.weather_app.modules.weather.controller.CachedWeatherController.*(..))", throwing = "exception")
    public void trackFailedApiCall(JoinPoint joinPoint, Exception exception) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String endpoint = "cached-weather/" + methodName;
            String errorType = exception.getClass().getSimpleName();

            // Get user ID from security context
            String userId = getCurrentUserId();

            // Track the error
            analyticsService.trackError(endpoint, errorType, userId);

        } catch (Exception e) {
            log.error("Error tracking API failure", e);
        }
    }

    /**
     * Track regular weather controller calls
     */
    @AfterReturning("execution(* com.weather_found.weather_app.modules.weather.controller.WeatherController.*(..))")
    public void trackRegularWeatherApiCall(JoinPoint joinPoint) {
        try {
            String methodName = joinPoint.getSignature().getName();
            String endpoint = "weather/" + methodName;

            // Get location parameter if available
            String location = extractLocationParameter(joinPoint);

            // Get user ID from security context
            String userId = getCurrentUserId();

            // Track the API request
            analyticsService.trackApiRequest(endpoint, location, userId);

        } catch (Exception e) {
            log.error("Error tracking regular weather API call", e);
        }
    }

    /**
     * Extract location parameter from method arguments
     */
    private String extractLocationParameter(JoinPoint joinPoint) {
        try {
            Object[] args = joinPoint.getArgs();
            if (args != null && args.length > 0) {
                // Look for String parameter that might be location
                for (Object arg : args) {
                    if (arg instanceof String) {
                        String str = (String) arg;
                        // Skip if it's clearly not a location (like date formats)
                        if (!str.matches("\\d{4}-\\d{2}-\\d{2}") &&
                                !str.equals("me") &&
                                str.length() > 1) {
                            return str;
                        }
                    }
                }
            }

            // Try to get location from request parameters
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                String location = request.getParameter("location");
                if (location != null && !location.isEmpty()) {
                    return location;
                }
            }

        } catch (Exception e) {
            log.debug("Could not extract location parameter", e);
        }
        return null;
    }

    /**
     * Get current user ID from security context
     */
    private String getCurrentUserId() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                return authentication.getName();
            }
        } catch (Exception e) {
            log.debug("Could not get current user ID", e);
        }
        return "anonymous";
    }
}
