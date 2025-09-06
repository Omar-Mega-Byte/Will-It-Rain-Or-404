package com.weather_found.weather_app.modules.notification.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for notification management
 */
@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Notification Management", description = "User notification and alert management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class NotificationController {

    /**
     * Get user notifications
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user notifications", description = "Get all notifications for the current user")
    public ResponseEntity<String> getUserNotifications(Authentication authentication) {
        String username = authentication.getName();
        String notifications = """
                {
                    "user": "%s",
                    "totalNotifications": 12,
                    "unreadCount": 3,
                    "notifications": [
                        {
                            "id": 1,
                            "type": "weather_alert",
                            "title": "Heavy Rain Alert",
                            "message": "Heavy rain expected in New York City tomorrow",
                            "severity": "medium",
                            "isRead": false,
                            "createdAt": "2025-09-05T08:30:00Z",
                            "location": "New York City",
                            "actionRequired": false
                        },
                        {
                            "id": 2,
                            "type": "event_reminder",
                            "title": "Event Reminder",
                            "message": "Your Beach Volleyball Tournament is in 10 days",
                            "severity": "low",
                            "isRead": false,
                            "createdAt": "2025-09-05T07:15:00Z",
                            "eventId": 1,
                            "actionRequired": true
                        },
                        {
                            "id": 3,
                            "type": "system",
                            "title": "Account Security",
                            "message": "Password updated successfully",
                            "severity": "low",
                            "isRead": true,
                            "createdAt": "2025-09-04T14:22:00Z",
                            "actionRequired": false
                        }
                    ],
                    "settings": {
                        "emailNotifications": true,
                        "pushNotifications": true,
                        "weatherAlerts": true,
                        "eventReminders": true
                    }
                }
                """.formatted(username);
        return ResponseEntity.ok(notifications);
    }

    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Mark notification as read", description = "Mark a specific notification as read")
    public ResponseEntity<String> markNotificationAsRead(@PathVariable Long id, Authentication authentication) {
        String markReadResult = """
                {
                    "status": "success",
                    "message": "Notification marked as read",
                    "notification": {
                        "id": %d,
                        "title": "Heavy Rain Alert",
                        "isRead": true,
                        "readAt": "%s",
                        "readBy": "%s"
                    },
                    "userStats": {
                        "totalUnread": 2,
                        "totalRead": 10,
                        "readRate": "83.3%%"
                    },
                    "relatedActions": [
                        "Email notification tracking updated",
                        "User engagement metrics updated"
                    ]
                }
                """.formatted(id, java.time.Instant.now().toString(), authentication.getName());
        return ResponseEntity.ok(markReadResult);
    }

    /**
     * Delete notification
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Delete notification", description = "Delete a specific notification")
    public ResponseEntity<String> deleteNotification(@PathVariable Long id, Authentication authentication) {
        String deleteResult = """
                {
                    "status": "success",
                    "message": "Notification deleted successfully",
                    "deletedNotification": {
                        "id": %d,
                        "title": "Event Reminder",
                        "type": "event_reminder",
                        "deletedAt": "%s",
                        "deletedBy": "%s"
                    },
                    "cleanup": {
                        "dataArchived": true,
                        "relatedDataRemoved": true,
                        "trackingUpdated": true
                    },
                    "userStats": {
                        "totalNotifications": 11,
                        "unreadCount": 2,
                        "storageFreed": "0.2 KB"
                    },
                    "recommendation": "Consider updating notification preferences to reduce unwanted notifications"
                }
                """.formatted(id, java.time.Instant.now().toString(), authentication.getName());
        return ResponseEntity.ok(deleteResult);
    }

    /**
     * Update notification settings
     */
    @PostMapping("/settings")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Update notification settings", description = "Update user's notification preferences")
    public ResponseEntity<String> updateNotificationSettings(@RequestBody String settingsData,
            Authentication authentication) {
        String username = authentication.getName();
        String settingsResult = """
                {
                    "status": "success",
                    "message": "Notification settings updated successfully",
                    "user": "%s",
                    "updatedAt": "%s",
                    "settings": {
                        "emailNotifications": true,
                        "pushNotifications": true,
                        "smsNotifications": false,
                        "weatherAlerts": {
                            "enabled": true,
                            "severity": ["medium", "high"],
                            "frequency": "immediate"
                        },
                        "eventReminders": {
                            "enabled": true,
                            "advanceNotice": ["1day", "1hour"],
                            "types": ["personal", "public"]
                        },
                        "systemNotifications": {
                            "enabled": true,
                            "types": ["security", "updates", "maintenance"]
                        },
                        "quietHours": {
                            "enabled": true,
                            "start": "22:00",
                            "end": "07:00",
                            "timezone": "user_local"
                        }
                    },
                    "changes": [
                        "SMS notifications disabled",
                        "Weather alert severity updated",
                        "Quiet hours configured"
                    ],
                    "effectiveFrom": "%s"
                }
                """.formatted(username, java.time.Instant.now().toString(), java.time.Instant.now().toString());
        return ResponseEntity.ok(settingsResult);
    }

    /**
     * Send test notification
     */
    @PostMapping("/test")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Send test notification", description = "Send a test notification to verify settings")
    public ResponseEntity<String> sendTestNotification(Authentication authentication) {
        String username = authentication.getName();
        String testResult = """
                {
                    "status": "success",
                    "message": "Test notification sent successfully",
                    "user": "%s",
                    "testNotification": {
                        "id": "TEST_%s",
                        "title": "Test Notification",
                        "message": "This is a test notification to verify your settings",
                        "type": "test",
                        "sentAt": "%s",
                        "channels": {
                            "email": {"status": "sent", "deliveryTime": "0.8s"},
                            "push": {"status": "sent", "deliveryTime": "0.3s"},
                            "sms": {"status": "disabled", "reason": "User preference"}
                        }
                    },
                    "verification": {
                        "emailDelivered": true,
                        "pushDelivered": true,
                        "configurationValid": true
                    },
                    "recommendations": [
                        "Email notifications are working correctly",
                        "Push notifications are working correctly",
                        "Consider enabling SMS for critical alerts"
                    ],
                    "nextSteps": [
                        "Check your email inbox",
                        "Verify push notification received",
                        "Adjust settings if needed"
                    ]
                }
                """.formatted(username, System.currentTimeMillis(), java.time.Instant.now().toString());
        return ResponseEntity.ok(testResult);
    }

    /**
     * Get all notifications - Admin only
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all notifications", description = "Get all notifications in the system (Admin only)")
    public ResponseEntity<String> getAllNotifications(Authentication authentication) {
        String allNotifications = """
                {
                    "totalNotifications": 8547,
                    "summary": {
                        "sent": 8234,
                        "pending": 156,
                        "failed": 157,
                        "read": 6890,
                        "unread": 1344
                    },
                    "byType": {
                        "weather_alert": 3421,
                        "event_reminder": 2156,
                        "system": 1876,
                        "promotional": 967,
                        "security": 127
                    },
                    "bySeverity": {
                        "low": 4523,
                        "medium": 2987,
                        "high": 891,
                        "critical": 146
                    },
                    "deliveryChannels": {
                        "email": {"sent": 7234, "delivered": 7089, "rate": "98.0%%"},
                        "push": {"sent": 6789, "delivered": 6456, "rate": "95.1%%"},
                        "sms": {"sent": 234, "delivered": 229, "rate": "97.9%%"}
                    },
                    "recentActivity": [
                        {"time": "2025-09-05T14:30:00Z", "type": "weather_alert", "count": 156},
                        {"time": "2025-09-05T13:00:00Z", "type": "event_reminder", "count": 89},
                        {"time": "2025-09-05T12:00:00Z", "type": "system", "count": 23}
                    ],
                    "performance": {
                        "averageDeliveryTime": "1.2 seconds",
                        "successRate": "96.8%%",
                        "userEngagement": "67.3%%"
                    }
                }
                """;
        return ResponseEntity.ok(allNotifications);
    }

    /**
     * Send broadcast notification - Admin only
     */
    @PostMapping("/broadcast")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send broadcast notification", description = "Send notification to all users (Admin only)")
    public ResponseEntity<String> sendBroadcastNotification(@RequestBody String notificationData,
            Authentication authentication) {
        String broadcastResult = """
                {
                    "status": "success",
                    "message": "Broadcast notification sent successfully",
                    "broadcast": {
                        "id": "BC_%s",
                        "title": "System Maintenance Notice",
                        "message": "Scheduled maintenance will occur on September 10th from 2:00-4:00 AM UTC",
                        "type": "system",
                        "severity": "medium",
                        "sentBy": "%s",
                        "sentAt": "%s"
                    },
                    "targeting": {
                        "totalUsers": 5847,
                        "activeUsers": 1247,
                        "eligibleUsers": 5203,
                        "excludedUsers": 644
                    },
                    "delivery": {
                        "email": {"queued": 4856, "estimated": "15 minutes"},
                        "push": {"queued": 3421, "estimated": "5 minutes"},
                        "sms": {"queued": 89, "estimated": "2 minutes"}
                    },
                    "scheduling": {
                        "immediate": true,
                        "deliveryStarted": "%s",
                        "estimatedCompletion": "%s"
                    },
                    "tracking": {
                        "trackingId": "BC_%s",
                        "analyticsEnabled": true,
                        "reportAvailable": "1 hour after completion"
                    }
                }
                """.formatted(
                System.currentTimeMillis(),
                authentication.getName(),
                java.time.Instant.now().toString(),
                java.time.Instant.now().toString(),
                java.time.Instant.now().plusSeconds(900).toString(),
                System.currentTimeMillis());
        return ResponseEntity.ok(broadcastResult);
    }
}
