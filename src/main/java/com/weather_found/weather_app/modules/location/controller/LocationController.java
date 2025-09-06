package com.weather_found.weather_app.modules.location.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for location management
 */
@RestController
@RequestMapping("/api/locations")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Location Management", description = "Geographic location management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class LocationController {

    /**
     * Search for locations
     */
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Search locations", description = "Search for locations by name or coordinates")
    public ResponseEntity<String> searchLocations(@RequestParam String query, Authentication authentication) {
        String searchResults = """
                {
                    "query": "%s",
                    "totalResults": 15,
                    "executionTime": "0.2 seconds",
                    "locations": [
                        {
                            "id": 1,
                            "name": "New York City",
                            "country": "United States",
                            "state": "New York",
                            "coordinates": {"lat": 40.7128, "lng": -74.0060},
                            "population": 8336817,
                            "timezone": "America/New_York",
                            "relevanceScore": 0.95
                        },
                        {
                            "id": 2,
                            "name": "Newark",
                            "country": "United States",
                            "state": "New Jersey",
                            "coordinates": {"lat": 40.7357, "lng": -74.1724},
                            "population": 311549,
                            "timezone": "America/New_York",
                            "relevanceScore": 0.78
                        },
                        {
                            "id": 3,
                            "name": "New Orleans",
                            "country": "United States",
                            "state": "Louisiana",
                            "coordinates": {"lat": 29.9511, "lng": -90.0715},
                            "population": 390144,
                            "timezone": "America/Chicago",
                            "relevanceScore": 0.72
                        }
                    ],
                    "suggestions": ["New York", "Newark", "New Delhi", "Newcastle"]
                }
                """.formatted(query);
        return ResponseEntity.ok(searchResults);
    }

    /**
     * Get location details
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get location details", description = "Get detailed information about a specific location")
    public ResponseEntity<String> getLocationDetails(@PathVariable Long id, Authentication authentication) {
        String locationDetails = """
                {
                    "id": %d,
                    "name": "New York City",
                    "fullName": "New York City, New York, United States",
                    "country": "United States",
                    "state": "New York",
                    "county": "New York County",
                    "coordinates": {
                        "latitude": 40.7128,
                        "longitude": -74.0060,
                        "elevation": "10 meters"
                    },
                    "demographics": {
                        "population": 8336817,
                        "area": "783.8 km²",
                        "density": "10,630/km²"
                    },
                    "timezone": {
                        "name": "America/New_York",
                        "offset": "UTC-5",
                        "currentTime": "%s"
                    },
                    "climate": {
                        "type": "Humid subtropical",
                        "averageTemperature": {
                            "summer": "25°C",
                            "winter": "4°C"
                        },
                        "averageRainfall": "1,268 mm/year"
                    },
                    "landmarks": [
                        "Statue of Liberty",
                        "Empire State Building",
                        "Central Park",
                        "Times Square"
                    ],
                    "nearbyLocations": [
                        {"name": "Brooklyn", "distance": "5 km"},
                        {"name": "Jersey City", "distance": "8 km"},
                        {"name": "Newark", "distance": "15 km"}
                    ]
                }
                """.formatted(id, java.time.Instant.now().toString());
        return ResponseEntity.ok(locationDetails);
    }

    /**
     * Get user's favorite locations
     */
    @GetMapping("/favorites")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get favorite locations", description = "Get user's favorite locations")
    public ResponseEntity<String> getFavoriteLocations(Authentication authentication) {
        String username = authentication.getName();
        String favoriteLocations = """
                {
                    "user": "%s",
                    "totalFavorites": 5,
                    "maxFavorites": 10,
                    "favorites": [
                        {
                            "id": 1,
                            "name": "New York City",
                            "country": "United States",
                            "coordinates": {"lat": 40.7128, "lng": -74.0060},
                            "addedAt": "2025-08-15T10:30:00Z",
                            "lastChecked": "2025-09-05T14:20:00Z",
                            "currentWeather": "22°C, Sunny"
                        },
                        {
                            "id": 2,
                            "name": "London",
                            "country": "United Kingdom",
                            "coordinates": {"lat": 51.5074, "lng": -0.1278},
                            "addedAt": "2025-08-20T16:45:00Z",
                            "lastChecked": "2025-09-05T14:20:00Z",
                            "currentWeather": "18°C, Cloudy"
                        },
                        {
                            "id": 3,
                            "name": "Tokyo",
                            "country": "Japan",
                            "coordinates": {"lat": 35.6762, "lng": 139.6503},
                            "addedAt": "2025-09-01T09:15:00Z",
                            "lastChecked": "2025-09-05T14:20:00Z",
                            "currentWeather": "26°C, Partly Cloudy"
                        }
                    ],
                    "quickStats": {
                        "mostChecked": "New York City",
                        "recentlyAdded": "Tokyo",
                        "averageCheckFrequency": "3.2 times/day"
                    }
                }
                """.formatted(username);
        return ResponseEntity.ok(favoriteLocations);
    }

    /**
     * Add location to favorites
     */
    @PostMapping("/favorites")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Add favorite location", description = "Add a location to user's favorites")
    public ResponseEntity<String> addFavoriteLocation(@RequestParam Long locationId, Authentication authentication) {
        String username = authentication.getName();
        String addResult = """
                {
                    "status": "success",
                    "message": "Location added to favorites successfully",
                    "user": "%s",
                    "addedLocation": {
                        "id": %d,
                        "name": "Paris",
                        "country": "France",
                        "coordinates": {"lat": 48.8566, "lng": 2.3522},
                        "addedAt": "%s"
                    },
                    "totalFavorites": 6,
                    "maxFavorites": 10,
                    "remainingSlots": 4,
                    "notification": {
                        "enabled": true,
                        "message": "You will now receive weather updates for Paris"
                    }
                }
                """.formatted(username, locationId, java.time.Instant.now().toString());
        return ResponseEntity.ok(addResult);
    }

    /**
     * Remove location from favorites
     */
    @DeleteMapping("/favorites/{id}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Remove favorite location", description = "Remove a location from user's favorites")
    public ResponseEntity<String> removeFavoriteLocation(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        String removeResult = """
                {
                    "status": "success",
                    "message": "Location removed from favorites successfully",
                    "user": "%s",
                    "removedLocation": {
                        "id": %d,
                        "name": "Berlin",
                        "country": "Germany",
                        "removedAt": "%s"
                    },
                    "totalFavorites": 5,
                    "maxFavorites": 10,
                    "remainingSlots": 5,
                    "notification": {
                        "message": "Weather updates for Berlin have been disabled"
                    },
                    "suggestion": "You can re-add this location anytime from search results"
                }
                """.formatted(username, id, java.time.Instant.now().toString());
        return ResponseEntity.ok(removeResult);
    }

    /**
     * Create custom location - Admin only
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create location", description = "Create a new custom location (Admin only)")
    public ResponseEntity<String> createLocation(@RequestBody String locationData, Authentication authentication) {
        String createdLocation = """
                {
                    "status": "success",
                    "message": "Custom location created successfully",
                    "location": {
                        "id": 501,
                        "name": "Custom Weather Station",
                        "type": "custom",
                        "coordinates": {"lat": 45.5017, "lng": -73.5673},
                        "address": "Custom Location, Montreal, QC",
                        "createdBy": "%s",
                        "createdAt": "%s",
                        "timezone": "America/Montreal",
                        "features": [
                            "Weather monitoring",
                            "Historical data collection",
                            "Custom alerts"
                        ]
                    },
                    "services": {
                        "weatherDataCollection": "enabled",
                        "publicAccess": "enabled",
                        "apiAccess": "enabled"
                    },
                    "monitoring": {
                        "sensors": ["temperature", "humidity", "pressure", "wind"],
                        "dataInterval": "5 minutes",
                        "qualityCheck": "automated"
                    },
                    "nextSteps": [
                        "Configure weather sensors",
                        "Set up data collection",
                        "Enable public visibility"
                    ]
                }
                """.formatted(authentication.getName(), java.time.Instant.now().toString());
        return ResponseEntity.ok(createdLocation);
    }
}
