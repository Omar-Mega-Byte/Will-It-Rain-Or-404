
package com.weather_found.weather_app.modules.location.controller;

import com.weather_found.weather_app.modules.location.dto.ml.LocationMLRequest;
import com.weather_found.weather_app.modules.location.dto.request.CreateLocationRequest;
import com.weather_found.weather_app.modules.location.dto.request.UpdateLocationRequest;
import com.weather_found.weather_app.modules.location.dto.response.LocationResponse;
import com.weather_found.weather_app.modules.location.dto.response.LocationSummaryResponse;
import com.weather_found.weather_app.modules.location.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Location Management
 */
@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Location Management", description = "Endpoints for managing geographic locations")
@SecurityRequirement(name = "bearerAuth")
public class LocationController {
        /**
         * Get location by ID
         * 
         * @param id Location ID
         * @return LocationResponse
         */
        @GetMapping("/{id}")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        @Operation(summary = "Get location by ID", description = "Retrieve a location by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved location"),
                        @ApiResponse(responseCode = "404", description = "Location not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<LocationResponse> getLocationById(
                        @Parameter(description = "Location ID", required = true) @PathVariable Long id) {
                LocationResponse location = locationService.getLocationById(id);
                return ResponseEntity.ok(location);
        }

        /**
         * Get location for ML
         * 
         * @return
         */
        @GetMapping("/ml/{id}")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        @Operation(summary = "Get location for ML by ID", description = "Retrieve a location for ML by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved location"),
                        @ApiResponse(responseCode = "404", description = "Location not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<LocationMLRequest> getLocationForMLById(
                        @Parameter(description = "Location ID", required = true) @PathVariable Long id) {
                LocationMLRequest location = locationService.getLocationForMLById(id);
                return ResponseEntity.ok(location);
        }

        /**
         * Get all locations
         */
        @GetMapping
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        @Operation(summary = "Get all locations", description = "Retrieve all locations")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved locations"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<List<LocationSummaryResponse>> getAllLocations() {
                List<LocationSummaryResponse> locations = locationService.getAllLocations();
                return ResponseEntity.ok(locations);
        }

        private final LocationService locationService;

        /**
         * Search locations by name, city, state, or country
         */
        @GetMapping("/search")
        @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
        @Operation(summary = "Search locations", description = "Search locations by name, city, state, or country")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully searched locations"),
                        @ApiResponse(responseCode = "400", description = "Invalid search parameters"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<List<LocationSummaryResponse>> searchLocations(
                        @Parameter(description = "Search query", required = true) @RequestParam @NotBlank String query) {
                log.info("Request to search locations with query: {}", query);
                List<LocationSummaryResponse> locations = locationService.searchLocations(query);
                return ResponseEntity.ok(locations);
        }

        /**
         * Create a new location
         */
        @PostMapping
        @PreAuthorize("hasAnyRole('ADMIN')")
        @Operation(summary = "Create location", description = "Create a new location")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Location created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "409", description = "Location already exists"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<LocationResponse> createLocation(
                        @Parameter(description = "Location data", required = true) @Valid @RequestBody CreateLocationRequest request) {
                log.info("Request to create location: {}", request.getName());
                LocationResponse location = locationService.createLocation(request);
                return ResponseEntity.status(HttpStatus.CREATED).body(location);
        }

        /**
         * Update an existing location
         */
        @PutMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN')")
        @Operation(summary = "Update location", description = "Update an existing location")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Location updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "404", description = "Location not found"),
                        @ApiResponse(responseCode = "409", description = "Location conflict"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<LocationResponse> updateLocation(
                        @Parameter(description = "Location ID", required = true) @PathVariable Long id,
                        @Parameter(description = "Updated location data", required = true) @Valid @RequestBody UpdateLocationRequest request) {
                log.info("Request to update location with ID: {}", id);
                LocationResponse location = locationService.updateLocation(id, request);
                return ResponseEntity.ok(location);
        }

        /**
         * Delete a location
         */
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAnyRole('ADMIN')")
        @Operation(summary = "Delete location", description = "Delete an existing location")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Location deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Location not found"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        public ResponseEntity<Void> deleteLocation(
                        @Parameter(description = "Location ID", required = true) @PathVariable Long id) {
                log.info("Request to delete location with ID: {}", id);
                locationService.deleteLocation(id);
                return ResponseEntity.noContent().build();
        }
}
