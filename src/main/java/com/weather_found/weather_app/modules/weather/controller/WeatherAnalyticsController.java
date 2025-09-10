package com.weather_found.weather_app.modules.weather.controller;

import com.weather_found.weather_app.modules.weather.service.WeatherAnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Weather Analytics Controller with Redis-based tracking
 */
@RestController
@RequestMapping("/api/weather/analytics")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Weather Analytics", description = "Weather API usage analytics and statistics")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class WeatherAnalyticsController {

    private final WeatherAnalyticsService analyticsService;

    /**
     * Get analytics dashboard
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get analytics dashboard", description = "Get comprehensive weather API analytics dashboard (Admin only)")
    public ResponseEntity<Map<String, Object>> getAnalyticsDashboard(Authentication authentication) {
        Map<String, Object> dashboard = analyticsService.getAnalyticsDashboard();
        return ResponseEntity.ok(dashboard);
    }

    /**
     * Get daily statistics
     */
    @GetMapping("/daily")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get daily statistics", description = "Get daily API usage statistics for a specific date")
    public ResponseEntity<Map<String, Object>> getDailyStats(
            @Parameter(description = "Date in YYYY-MM-DD format (defaults to today)") @RequestParam(required = false) String date,
            Authentication authentication) {

        if (date == null || date.isEmpty()) {
            date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        Map<String, Object> stats = analyticsService.getDailyStats(date);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get hourly statistics for today
     */
    @GetMapping("/hourly")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get hourly statistics", description = "Get hourly API usage statistics for today")
    public ResponseEntity<Map<String, Object>> getHourlyStats(Authentication authentication) {
        Map<String, Object> stats = analyticsService.getHourlyStatsToday();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get top endpoints by usage
     */
    @GetMapping("/endpoints/top")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get top endpoints", description = "Get most used API endpoints")
    public ResponseEntity<Map<String, Object>> getTopEndpoints(
            @Parameter(description = "Number of top endpoints to return") @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {

        Map<String, Object> stats = analyticsService.getTopEndpoints(limit);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get top requested locations
     */
    @GetMapping("/locations/top")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get top locations", description = "Get most requested weather locations")
    public ResponseEntity<Map<String, Object>> getTopLocations(
            @Parameter(description = "Number of top locations to return") @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {

        Map<String, Object> stats = analyticsService.getTopLocations(limit);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get user activity statistics
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get user activity", description = "Get API usage statistics for a specific user (Admin only)")
    public ResponseEntity<Map<String, Object>> getUserActivity(
            @PathVariable String userId,
            @Parameter(description = "Number of top activities to return") @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {

        Map<String, Object> stats = analyticsService.getUserActivity(userId, limit);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get current user's activity statistics
     */
    @GetMapping("/user/me")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get my activity", description = "Get API usage statistics for current user")
    public ResponseEntity<Map<String, Object>> getMyActivity(
            @Parameter(description = "Number of top activities to return") @RequestParam(defaultValue = "10") int limit,
            Authentication authentication) {

        String userId = authentication.getName();
        Map<String, Object> stats = analyticsService.getUserActivity(userId, limit);
        return ResponseEntity.ok(stats);
    }

    /**
     * Get error statistics
     */
    @GetMapping("/errors")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get error statistics", description = "Get API error statistics for a specific date")
    public ResponseEntity<Map<String, Object>> getErrorStats(
            @Parameter(description = "Date in YYYY-MM-DD format (defaults to today)") @RequestParam(required = false) String date,
            Authentication authentication) {

        if (date == null || date.isEmpty()) {
            date = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        }

        Map<String, Object> stats = analyticsService.getErrorStats(date);
        return ResponseEntity.ok(stats);
    }

    /**
     * Track a custom event
     */
    @PostMapping("/track")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Track custom event", description = "Track a custom analytics event")
    public ResponseEntity<Map<String, Object>> trackEvent(
            @RequestBody Map<String, Object> eventData,
            Authentication authentication) {

        String endpoint = (String) eventData.get("endpoint");
        String location = (String) eventData.get("location");
        String userId = authentication.getName();

        if (endpoint != null) {
            analyticsService.trackApiRequest(endpoint, location, userId);
        }

        Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Event tracked successfully",
                "event", eventData,
                "timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }

    /**
     * Reset analytics data (for testing/maintenance)
     */
    @DeleteMapping("/reset")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Reset analytics", description = "Reset all analytics data (Admin only - use with caution)")
    public ResponseEntity<Map<String, Object>> resetAnalytics(Authentication authentication) {
        analyticsService.resetAnalytics();

        Map<String, Object> response = Map.of(
                "status", "success",
                "message", "Analytics data reset successfully",
                "resetBy", authentication.getName(),
                "timestamp", java.time.LocalDateTime.now());

        return ResponseEntity.ok(response);
    }
}
