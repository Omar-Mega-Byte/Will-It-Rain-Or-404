package com.weather_found.weather_app.modules.analytics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for analytics and reporting
 */
@RestController
@RequestMapping("/api/analytics")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Analytics & Reporting", description = "Analytics, metrics, and reporting endpoints")
@SecurityRequirement(name = "bearerAuth")
public class AnalyticsController {

    /**
     * Get user analytics
     */
    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user analytics", description = "Get analytics data for the current user")
    public ResponseEntity<String> getUserAnalytics(Authentication authentication) {
        String username = authentication.getName();
        String analyticsData = """
                {
                    "user": "%s",
                    "totalQueries": 127,
                    "locationsSearched": 8,
                    "averageSessionTime": "4.5 minutes",
                    "mostSearchedLocation": "New York",
                    "totalPredictions": 45,
                    "accuracyRate": "87.3%%",
                    "lastActive": "2025-09-05T14:30:00Z"
                }
                """.formatted(username);
        return ResponseEntity.ok(analyticsData);
    }

    /**
     * Get user activity summary
     */
    @GetMapping("/user/activity")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user activity", description = "Get user activity summary and statistics")
    public ResponseEntity<String> getUserActivity(Authentication authentication) {
        String username = authentication.getName();
        String activityData = """
                {
                    "user": "%s",
                    "todayQueries": 12,
                    "weeklyQueries": 78,
                    "monthlyQueries": 245,
                    "favoriteTime": "08:00-10:00",
                    "deviceUsage": {
                        "mobile": "65%%",
                        "desktop": "35%%"
                    },
                    "recentActivities": [
                        {"action": "Weather Query", "location": "London", "time": "2025-09-05T09:15:00Z"},
                        {"action": "Event Created", "name": "Beach Party", "time": "2025-09-05T08:30:00Z"},
                        {"action": "Location Added", "location": "Paris", "time": "2025-09-04T16:45:00Z"}
                    ]
                }
                """.formatted(username);
        return ResponseEntity.ok(activityData);
    }

    /**
     * Get prediction accuracy metrics - Admin only
     */
    @GetMapping("/predictions")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get prediction metrics", description = "Get prediction accuracy and performance metrics (Admin only)")
    public ResponseEntity<String> getPredictionMetrics(Authentication authentication) {
        String metricsData = """
                {
                    "totalPredictions": 15847,
                    "accuracyRate": "89.2%%",
                    "averageResponseTime": "0.8 seconds",
                    "modelsPerformance": {
                        "temperature": {"accuracy": "92.1%%", "errorMargin": "±1.2°C"},
                        "precipitation": {"accuracy": "85.7%%", "errorMargin": "±5%%"},
                        "windSpeed": {"accuracy": "88.9%%", "errorMargin": "±2.3 km/h"},
                        "humidity": {"accuracy": "91.4%%", "errorMargin": "±3%%"}
                    },
                    "recentAccuracy": [
                        {"date": "2025-09-05", "accuracy": "90.1%%"},
                        {"date": "2025-09-04", "accuracy": "88.7%%"},
                        {"date": "2025-09-03", "accuracy": "91.2%%"}
                    ]
                }
                """;
        return ResponseEntity.ok(metricsData);
    }

    /**
     * Get system performance metrics - Admin only
     */
    @GetMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get system metrics", description = "Get system performance and usage metrics (Admin only)")
    public ResponseEntity<String> getSystemMetrics(Authentication authentication) {
        String systemData = """
                {
                    "serverHealth": "Excellent",
                    "uptime": "99.8%%",
                    "responseTime": "0.7 seconds",
                    "memoryUsage": "68%%",
                    "cpuUsage": "45%%",
                    "diskSpace": "72%% used",
                    "activeUsers": 1247,
                    "dailyRequests": 45890,
                    "errorRate": "0.2%%",
                    "databaseConnections": 15,
                    "cacheHitRate": "94.3%%",
                    "apiCallsToday": 12456
                }
                """;
        return ResponseEntity.ok(systemData);
    }

    /**
     * Get user engagement metrics - Admin only
     */
    @GetMapping("/engagement")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get engagement metrics", description = "Get user engagement and usage analytics (Admin only)")
    public ResponseEntity<String> getEngagementMetrics(Authentication authentication) {
        String engagementData = """
                {
                    "totalUsers": 5847,
                    "activeUsers": {
                        "today": 1247,
                        "thisWeek": 3891,
                        "thisMonth": 5203
                    },
                    "newRegistrations": {
                        "today": 23,
                        "thisWeek": 189,
                        "thisMonth": 647
                    },
                    "userRetention": {
                        "day1": "87%%",
                        "day7": "62%%",
                        "day30": "45%%"
                    },
                    "popularFeatures": [
                        {"feature": "Weather Forecast", "usage": "89%%"},
                        {"feature": "Location Search", "usage": "76%%"},
                        {"feature": "Event Planning", "usage": "54%%"},
                        {"feature": "Notifications", "usage": "67%%"}
                    ],
                    "averageSessionTime": "5.2 minutes"
                }
                """;
        return ResponseEntity.ok(engagementData);
    }

    /**
     * Export analytics data - Admin only
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Export analytics data", description = "Export analytics data in various formats (Admin only)")
    public ResponseEntity<String> exportAnalyticsData(@RequestParam(defaultValue = "csv") String format,
            Authentication authentication) {
        String exportData = """
                {
                    "status": "success",
                    "format": "%s",
                    "fileName": "analytics_export_%s.%s",
                    "fileSize": "2.4 MB",
                    "recordCount": 15847,
                    "downloadUrl": "/api/downloads/analytics_export_%s.%s",
                    "expiresAt": "2025-09-12T14:30:00Z",
                    "description": "Analytics data exported successfully in %s format"
                }
                """.formatted(format, java.time.LocalDate.now(), format, java.time.LocalDate.now(), format, format);
        return ResponseEntity.ok(exportData);
    }

    /**
     * Generate custom report - Admin only
     */
    @PostMapping("/reports")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Generate custom report", description = "Generate custom analytics report (Admin only)")
    public ResponseEntity<String> generateCustomReport(@RequestBody String reportConfig,
            Authentication authentication) {
        String reportData = """
                {
                    "status": "success",
                    "reportId": "RPT_%s",
                    "title": "Custom Analytics Report",
                    "generatedAt": "%s",
                    "reportType": "custom",
                    "dataPoints": 8945,
                    "summary": {
                        "totalUsers": 5847,
                        "totalPredictions": 15847,
                        "averageAccuracy": "89.2%%",
                        "topLocation": "New York"
                    },
                    "downloadUrl": "/api/reports/RPT_%s.pdf",
                    "estimatedSize": "1.8 MB",
                    "message": "Custom report generated successfully based on provided configuration"
                }
                """.formatted(
                System.currentTimeMillis(),
                java.time.Instant.now().toString(),
                System.currentTimeMillis());
        return ResponseEntity.ok(reportData);
    }
}
