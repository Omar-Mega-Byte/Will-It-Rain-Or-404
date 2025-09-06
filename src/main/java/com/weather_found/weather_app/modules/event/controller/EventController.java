package com.weather_found.weather_app.modules.event.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for event management
 */
@RestController
@RequestMapping("/api/events")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Event Management", description = "Outdoor event planning and management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class EventController {

    /**
     * Get user's events
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get user events", description = "Get all events for the current user")
    public ResponseEntity<String> getUserEvents(Authentication authentication) {
        String username = authentication.getName();
        String eventsData = """
                {
                    "user": "%s",
                    "totalEvents": 8,
                    "upcomingEvents": 3,
                    "events": [
                        {
                            "id": 1,
                            "title": "Beach Volleyball Tournament",
                            "date": "2025-09-15",
                            "time": "10:00",
                            "location": "Santa Monica Beach",
                            "status": "confirmed",
                            "weatherForecast": "Sunny, 24°C"
                        },
                        {
                            "id": 2,
                            "title": "Hiking Adventure",
                            "date": "2025-09-20",
                            "time": "07:00",
                            "location": "Yosemite National Park",
                            "status": "pending",
                            "weatherForecast": "Partly cloudy, 18°C"
                        },
                        {
                            "id": 3,
                            "title": "Outdoor Concert",
                            "date": "2025-09-25",
                            "time": "19:00",
                            "location": "Central Park",
                            "status": "confirmed",
                            "weatherForecast": "Clear, 22°C"
                        }
                    ]
                }
                """.formatted(username);
        return ResponseEntity.ok(eventsData);
    }

    /**
     * Create new event
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Create event", description = "Create a new outdoor event")
    public ResponseEntity<String> createEvent(@RequestBody String eventData, Authentication authentication) {
        String username = authentication.getName();
        String createdEvent = """
                {
                    "status": "success",
                    "message": "Event created successfully",
                    "event": {
                        "id": 9,
                        "title": "New Outdoor Event",
                        "createdBy": "%s",
                        "createdAt": "%s",
                        "status": "pending",
                        "location": "TBD",
                        "date": "2025-09-30",
                        "time": "14:00",
                        "attendeesCount": 0,
                        "weatherPrediction": "Will be updated closer to date"
                    },
                    "nextSteps": [
                        "Add event details",
                        "Invite participants",
                        "Monitor weather forecast"
                    ]
                }
                """.formatted(username, java.time.Instant.now().toString());
        return ResponseEntity.ok(createdEvent);
    }

    /**
     * Get event details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get event details", description = "Get detailed information about a specific event")
    public ResponseEntity<String> getEventDetails(@PathVariable Long id, Authentication authentication) {
        String eventDetails = """
                {
                    "id": %d,
                    "title": "Beach Volleyball Tournament",
                    "description": "Annual beach volleyball tournament for all skill levels",
                    "date": "2025-09-15",
                    "time": "10:00",
                    "duration": "6 hours",
                    "location": {
                        "name": "Santa Monica Beach",
                        "address": "Santa Monica, CA 90401",
                        "coordinates": {"lat": 34.0195, "lng": -118.4912}
                    },
                    "organizer": "beach_volleyball_club",
                    "status": "confirmed",
                    "attendees": {
                        "registered": 24,
                        "limit": 32,
                        "waitlist": 3
                    },
                    "weatherForecast": {
                        "condition": "Sunny",
                        "temperature": "24°C",
                        "humidity": "65%%",
                        "windSpeed": "12 km/h",
                        "chanceOfRain": "5%%"
                    },
                    "equipment": ["Volleyball nets", "Balls", "First aid kit"],
                    "requirements": ["Swimwear", "Sunscreen", "Water bottle"]
                }
                """.formatted(id);
        return ResponseEntity.ok(eventDetails);
    }

    /**
     * Update event
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Update event", description = "Update an existing event")
    public ResponseEntity<String> updateEvent(@PathVariable Long id, @RequestBody String eventData,
            Authentication authentication) {
        String updatedEvent = """
                {
                    "status": "success",
                    "message": "Event updated successfully",
                    "event": {
                        "id": %d,
                        "title": "Updated Event Title",
                        "lastModified": "%s",
                        "modifiedBy": "%s",
                        "changes": [
                            "Title updated",
                            "Location changed",
                            "Time modified"
                        ]
                    },
                    "notifications": {
                        "attendeesNotified": true,
                        "emailsSent": 24,
                        "pushNotificationsSent": 18
                    }
                }
                """.formatted(id, java.time.Instant.now().toString(), authentication.getName());
        return ResponseEntity.ok(updatedEvent);
    }

    /**
     * Delete event
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Delete event", description = "Delete an existing event")
    public ResponseEntity<String> deleteEvent(@PathVariable Long id, Authentication authentication) {
        String deletionResult = """
                {
                    "status": "success",
                    "message": "Event deleted successfully",
                    "deletedEvent": {
                        "id": %d,
                        "title": "Deleted Event",
                        "deletedAt": "%s",
                        "deletedBy": "%s"
                    },
                    "cleanup": {
                        "attendeesNotified": true,
                        "refundsProcessed": 0,
                        "dataArchived": true
                    },
                    "relatedActions": [
                        "Notification sent to 24 attendees",
                        "Calendar invites cancelled",
                        "Resources deallocated"
                    ]
                }
                """.formatted(id, java.time.Instant.now().toString(), authentication.getName());
        return ResponseEntity.ok(deletionResult);
    }

    /**
     * Get weather prediction for event
     */
    @GetMapping("/{id}/weather")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get event weather prediction", description = "Get weather prediction for a specific event")
    public ResponseEntity<String> getEventWeatherPrediction(@PathVariable Long id, Authentication authentication) {
        String weatherPrediction = """
                {
                    "eventId": %d,
                    "eventTitle": "Beach Volleyball Tournament",
                    "eventDate": "2025-09-15",
                    "eventTime": "10:00",
                    "location": "Santa Monica Beach",
                    "prediction": {
                        "accuracy": "89%%",
                        "confidence": "high",
                        "lastUpdated": "%s",
                        "forecast": {
                            "condition": "Sunny",
                            "temperature": {
                                "min": "20°C",
                                "max": "26°C",
                                "average": "24°C"
                            },
                            "precipitation": {
                                "chanceOfRain": "5%%",
                                "expectedAmount": "0mm"
                            },
                            "wind": {
                                "speed": "12 km/h",
                                "direction": "Southwest",
                                "gusts": "18 km/h"
                            },
                            "humidity": "65%%",
                            "uvIndex": "7 (High)"
                        }
                    },
                    "recommendations": [
                        "Perfect conditions for outdoor activities",
                        "Bring sunscreen and water",
                        "Light wind may affect ball trajectory slightly"
                    ]
                }
                """.formatted(id, java.time.Instant.now().toString());
        return ResponseEntity.ok(weatherPrediction);
    }

    /**
     * Get all events - Admin only
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all events", description = "Get all events in the system (Admin only)")
    public ResponseEntity<String> getAllEvents(Authentication authentication) {
        String allEventsData = """
                {
                    "totalEvents": 156,
                    "activeEvents": 89,
                    "completedEvents": 67,
                    "upcomingEvents": 34,
                    "events": [
                        {
                            "id": 1,
                            "title": "Beach Volleyball Tournament",
                            "organizer": "user123",
                            "date": "2025-09-15",
                            "location": "Santa Monica Beach",
                            "status": "confirmed",
                            "attendees": 24
                        },
                        {
                            "id": 2,
                            "title": "Mountain Hiking Trip",
                            "organizer": "user456",
                            "date": "2025-09-20",
                            "location": "Yosemite National Park",
                            "status": "pending",
                            "attendees": 12
                        },
                        {
                            "id": 3,
                            "title": "Outdoor Music Festival",
                            "organizer": "user789",
                            "date": "2025-09-25",
                            "location": "Central Park",
                            "status": "confirmed",
                            "attendees": 156
                        }
                    ],
                    "statistics": {
                        "averageAttendees": 18.5,
                        "popularLocations": ["Central Park", "Santa Monica Beach", "Golden Gate Park"],
                        "eventsByMonth": {"September": 45, "October": 38, "November": 23}
                    }
                }
                """;
        return ResponseEntity.ok(allEventsData);
    }
}
