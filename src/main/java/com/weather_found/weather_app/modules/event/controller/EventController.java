package com.weather_found.weather_app.modules.event.controller;

import com.weather_found.weather_app.modules.event.dto.request.*;
import com.weather_found.weather_app.modules.event.dto.response.*;
import com.weather_found.weather_app.modules.event.service.EventService;
import com.weather_found.weather_app.modules.shared.Base.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;
import java.time.LocalDateTime;
import java.util.List;

/**
 * REST Controller for Event Management
 * Provides comprehensive CRUD operations and event features
 *
 * @author Weather App Team
 * @version 1.0
 */
@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
@Validated
@Tag(name = "Event Management", description = "Event CRUD operations and management features")
public class EventController {

        private final EventService eventService;

        // ==================== CREATE OPERATIONS ====================

        /**
         * Create a new event
         */
        @PostMapping
        @Operation(summary = "Create a new event", description = "Creates a new event with the provided details")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "Event created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "409", description = "Event conflict exists"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<BaseResponse<EventResponseDto>> createEvent(
                        @Valid @RequestBody EventCreateDto eventCreateDto,
                        Authentication authentication) {

                String username = authentication.getName();

                EventResponseDto createdEvent = eventService.createEvent(eventCreateDto, username);
                BaseResponse<EventResponseDto> response = BaseResponse.<EventResponseDto>builder()
                                .success(true)
                                .message("Event created successfully")
                                .data(createdEvent)
                                .build();

                return new ResponseEntity<>(response, HttpStatus.CREATED);
        }

        // ==================== READ OPERATIONS ====================

        /**
         * Get event by ID
         */
        @GetMapping("/{eventId}")
        @Operation(summary = "Get event by ID", description = "Retrieves an event by its unique identifier")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Event found and returned"),
                        @ApiResponse(responseCode = "404", description = "Event not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<BaseResponse<EventResponseDto>> getEventById(
                        @Parameter(description = "Event ID", required = true, example = "1") @PathVariable Long eventId,
                        Authentication authentication) {

                String username = authentication.getName();

                EventResponseDto event = eventService.getEventById(eventId, username);
                BaseResponse<EventResponseDto> response = BaseResponse.<EventResponseDto>builder()
                                .success(true)
                                .message("Event retrieved successfully")
                                .data(event)
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * Get current user's events with pagination
         */
        @GetMapping
        @Operation(summary = "Get user events", description = "Retrieves paginated list of current user's events")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<BaseResponse<EventPageResponseDto>> getUserEvents(
                        @Parameter(description = "Page number (0-based)", example = "0") @RequestParam(defaultValue = "0") @Min(0) int page,

                        @Parameter(description = "Number of items per page", example = "10") @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size,

                        Authentication authentication) {

                String username = authentication.getName();

                Pageable pageable = PageRequest.of(page, size);
                EventPageResponseDto events = eventService.getUserEventsPageable(username, pageable);

                BaseResponse<EventPageResponseDto> response = BaseResponse.<EventPageResponseDto>builder()
                                .success(true)
                                .message("Events retrieved successfully")
                                .data(events)
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * Get all user's events (no pagination)
         */
        @GetMapping("/all")
        @Operation(summary = "Get all user events", description = "Retrieves all events for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Events retrieved successfully"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<BaseResponse<List<EventSummaryDto>>> getAllUserEvents(
                        Authentication authentication) {

                String username = authentication.getName();

                List<EventSummaryDto> events = eventService.getUserEvents(username);

                BaseResponse<List<EventSummaryDto>> response = BaseResponse.<List<EventSummaryDto>>builder()
                                .success(true)
                                .message("All events retrieved successfully")
                                .data(events)
                                .build();

                return ResponseEntity.ok(response);
        }

        // ==================== UPDATE OPERATIONS ====================

        /**
         * Update an existing event
         */
        @PutMapping("/{eventId}")
        @Operation(summary = "Update event", description = "Updates an existing event with new details")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Event updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "404", description = "Event not found"),
                        @ApiResponse(responseCode = "409", description = "Event conflict exists"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<BaseResponse<EventResponseDto>> updateEvent(
                        @Parameter(description = "Event ID", required = true, example = "1") @PathVariable Long eventId,

                        @Valid @RequestBody EventUpdateDto eventUpdateDto,
                        Authentication authentication) {

                String username = authentication.getName();

                EventResponseDto updatedEvent = eventService.updateEvent(eventId, eventUpdateDto, username);

                BaseResponse<EventResponseDto> response = BaseResponse.<EventResponseDto>builder()
                                .success(true)
                                .message("Event updated successfully")
                                .data(updatedEvent)
                                .build();

                return ResponseEntity.ok(response);
        }

        // ==================== DELETE OPERATIONS ====================

        /**
         * Delete an event
         */
        @DeleteMapping("/{eventId}")
        @Operation(summary = "Delete event", description = "Deletes an event by its ID")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "Event deleted successfully"),
                        @ApiResponse(responseCode = "404", description = "Event not found"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<BaseResponse<MessageResponseDto>> deleteEvent(
                        @Parameter(description = "Event ID", required = true, example = "1") @PathVariable Long eventId,
                        Authentication authentication) {

                String username = authentication.getName();

                MessageResponseDto result = eventService.deleteEvent(eventId, username);

                BaseResponse<MessageResponseDto> response = BaseResponse.<MessageResponseDto>builder()
                                .success(true)
                                .message("Event deleted successfully")
                                .data(result)
                                .build();

                return ResponseEntity.ok(response);
        }

        // ==================== SEARCH OPERATIONS ====================

        /**
         * Search events
         */
        @PostMapping("/search")
        @Operation(summary = "Search events", description = "Searches events based on criteria")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Search completed successfully"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<BaseResponse<List<EventSummaryDto>>> searchEvents(
                        @Valid @RequestBody EventSearchDto searchDto,
                        Authentication authentication) {

                String username = authentication.getName();

                List<EventSummaryDto> events = eventService.searchEvents(searchDto, username);

                BaseResponse<List<EventSummaryDto>> response = BaseResponse.<List<EventSummaryDto>>builder()
                                .success(true)
                                .message("Search completed successfully")
                                .data(events)
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * Get upcoming events
         */
        @GetMapping("/upcoming")
        @Operation(summary = "Get upcoming events", description = "Retrieves events scheduled for the future")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Upcoming events retrieved successfully"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<BaseResponse<List<EventSummaryDto>>> getUpcomingEvents(
                        Authentication authentication) {

                String username = authentication.getName();

                List<EventSummaryDto> events = eventService.getUpcomingEvents(username);

                BaseResponse<List<EventSummaryDto>> response = BaseResponse.<List<EventSummaryDto>>builder()
                                .success(true)
                                .message("Upcoming events retrieved successfully")
                                .data(events)
                                .build();

                return ResponseEntity.ok(response);
        }

        /**
         * Get conflicting events
         */
        @GetMapping("/conflicts")
        @Operation(summary = "Get conflicting events", description = "Checks for events that conflict with the specified time range")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Conflict check completed successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid date parameters"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<BaseResponse<List<EventSummaryDto>>> getConflictingEvents(
                        @Parameter(description = "Start date and time", required = true) @RequestParam String startDate,

                        @Parameter(description = "End date and time", required = true) @RequestParam String endDate,

                        Authentication authentication) {

                String username = authentication.getName();

                LocalDateTime start = LocalDateTime.parse(startDate);
                LocalDateTime end = LocalDateTime.parse(endDate);

                List<EventSummaryDto> conflicts = eventService.getConflictingEvents(start, end, username);

                BaseResponse<List<EventSummaryDto>> response = BaseResponse.<List<EventSummaryDto>>builder()
                                .success(true)
                                .message("Conflict check completed successfully")
                                .data(conflicts)
                                .build();

                return ResponseEntity.ok(response);
        }

        // ==================== STATISTICS OPERATIONS ====================

        /**
         * Get event statistics for current user
         */
        @GetMapping("/stats")
        @Operation(summary = "Get user event statistics", description = "Retrieves event statistics for the current user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<BaseResponse<EventStatisticsDto>> getUserEventStats(
                        Authentication authentication) {

                String username = authentication.getName();

                EventStatisticsDto stats = eventService.getUserEventStatistics(username);

                BaseResponse<EventStatisticsDto> response = BaseResponse.<EventStatisticsDto>builder()
                                .success(true)
                                .message("Statistics retrieved successfully")
                                .data(stats)
                                .build();

                return ResponseEntity.ok(response);
        }

        // ==================== UTILITY OPERATIONS ====================

        /**
         * Check if event name is available
         */
        @GetMapping("/check-name")
        @Operation(summary = "Check event name availability", description = "Checks if an event name is available for the user")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Check completed successfully"),
                        @ApiResponse(responseCode = "500", description = "Internal server error")
        })
        public ResponseEntity<BaseResponse<Boolean>> checkEventNameAvailability(
                        @Parameter(description = "Event name to check", required = true) @RequestParam String eventName,
                        Authentication authentication) {

                String username = authentication.getName();

                boolean isAvailable = eventService.isEventNameAvailable(eventName, username);

                BaseResponse<Boolean> response = BaseResponse.<Boolean>builder()
                                .success(true)
                                .message("Check completed successfully")
                                .data(isAvailable)
                                .build();

                return ResponseEntity.ok(response);
        }
}
