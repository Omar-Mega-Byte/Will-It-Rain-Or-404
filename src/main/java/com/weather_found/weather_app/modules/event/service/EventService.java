package com.weather_found.weather_app.modules.event.service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.weather_found.weather_app.modules.event.dto.request.EventCreateDto;
import com.weather_found.weather_app.modules.event.dto.request.EventSearchDto;
import com.weather_found.weather_app.modules.event.dto.request.EventUpdateDto;
import com.weather_found.weather_app.modules.event.dto.response.EventPageResponseDto;
import com.weather_found.weather_app.modules.event.dto.response.EventResponseDto;
import com.weather_found.weather_app.modules.event.dto.response.EventStatisticsDto;
import com.weather_found.weather_app.modules.event.dto.response.EventSummaryDto;
import com.weather_found.weather_app.modules.event.dto.response.MessageResponseDto;
import com.weather_found.weather_app.modules.event.exception.EventNotFoundException;
import com.weather_found.weather_app.modules.event.exception.EventAccessDeniedException;
import com.weather_found.weather_app.modules.event.mapper.EventMapper;
import com.weather_found.weather_app.modules.event.model.Event;
import com.weather_found.weather_app.modules.event.model.enums.EventStatus;
import com.weather_found.weather_app.modules.event.repository.EventRepository;
import com.weather_found.weather_app.modules.event.validation.EventValidation;
import com.weather_found.weather_app.modules.user.model.User;
import com.weather_found.weather_app.modules.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Event Service - Complete event management functionality
 * 
 * This service handles all event operations including:
 * - CRUD operations with validation
 * - User permission management
 * - Event conflict detection
 * - Statistics and analytics
 * 
 * @author Weather Found Team
 * @version 1.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EventMapper eventMapper;
    private final EventValidation eventValidation;

    // ===============================
    // CREATE OPERATIONS
    // ===============================

    /**
     * Creates a new event for the authenticated user
     * 
     * @param createDto Event creation data
     * @param username  Current user's username
     * @return Created event details
     * @throws InvalidEventException  if validation fails
     * @throws EventConflictException if conflicts exist
     */
    @Transactional
    public EventResponseDto createEvent(EventCreateDto createDto, String username) {
        log.info("Creating new event '{}' for user '{}'", createDto.getEventName(), username);

        // Get user
        User user = getUserByUsername(username);

        // Get user's existing events for validation
        List<Event> userEvents = eventRepository.findByUserId(user.getId());

        // Validate event creation with conflicts
        eventValidation.validateEventCreationWithConflicts(createDto, userEvents);

        // Create and save event
        Event event = eventMapper.toEvent(createDto);
        Event savedEvent = eventRepository.save(event);

        // Add event to user's events list (User is the owning side of the relationship)
        if (user.getEvents() == null) {
            user.setEvents(new java.util.ArrayList<>());
        }
        user.getEvents().add(savedEvent);
        userRepository.save(user);

        log.info("Successfully created event with ID: {} for user '{}' and established user-event relationship",
                savedEvent.getId(), username);
        return eventMapper.toEventResponseDto(savedEvent);
    }

    // ===============================
    // READ OPERATIONS
    // ===============================

    /**
     * Gets event by ID for the authenticated user
     * 
     * @param eventId  Event ID
     * @param username Current user's username
     * @return Event details
     * @throws EventNotFoundException     if event not found
     * @throws EventAccessDeniedException if user has no access
     */
    public EventResponseDto getEventById(Long eventId, String username) {
        log.debug("Getting event {} for user '{}'", eventId, username);

        Event event = getEventByIdAndValidateAccess(eventId, username);
        return eventMapper.toEventResponseDto(event);
    }

    /**
     * Gets all events for the authenticated user
     * 
     * @param username Current user's username
     * @return List of user's events
     */
    public List<EventSummaryDto> getUserEvents(String username) {
        log.debug("Getting all events for user '{}'", username);

        User user = getUserByUsername(username);
        List<Event> events = eventRepository.findByUserId(user.getId());

        log.debug("Found {} events for user '{}'", events.size(), username);
        return eventMapper.toEventSummaryDtoList(events);
    }

    /**
     * Gets paginated events for the authenticated user
     * 
     * @param username Current user's username
     * @param pageable Pagination parameters
     * @return Paginated event response
     */
    public EventPageResponseDto getUserEventsPageable(String username, Pageable pageable) {
        log.debug("Getting paginated events for user '{}', page: {}, size: {}",
                username, pageable.getPageNumber(), pageable.getPageSize());

        User user = getUserByUsername(username);
        Page<Event> eventPage = eventRepository.findByUserId(user.getId(), pageable);

        log.debug("Found {} events (page {} of {}) for user '{}'",
                eventPage.getNumberOfElements(), eventPage.getNumber() + 1,
                eventPage.getTotalPages(), username);

        return eventMapper.toEventPageResponseDto(eventPage);
    }

    /**
     * Searches events for the authenticated user based on criteria
     * 
     * @param searchDto Search criteria
     * @param username  Current user's username
     * @return List of matching events
     */
    public List<EventSummaryDto> searchEvents(EventSearchDto searchDto, String username) {
        log.debug("Searching events for user '{}' with criteria: {}", username, searchDto);

        User user = getUserByUsername(username);
        List<Event> allUserEvents = eventRepository.findByUserId(user.getId());

        // Filter events based on search criteria
        List<Event> filteredEvents = allUserEvents.stream()
                .filter(event -> matchesSearchCriteria(event, searchDto))
                .toList();

        log.debug("Found {} events matching search criteria for user '{}'",
                filteredEvents.size(), username);

        return eventMapper.toEventSummaryDtoList(filteredEvents);
    }

    /**
     * Gets upcoming events for the authenticated user
     * 
     * @param username Current user's username
     * @return List of upcoming events
     */
    public List<EventSummaryDto> getUpcomingEvents(String username) {
        log.debug("Getting upcoming events for user '{}'", username);

        User user = getUserByUsername(username);
        List<Event> upcomingEvents = eventRepository.findUpcomingEventsByUserId(
                user.getId(), LocalDateTime.now(), EventStatus.SCHEDULED);

        log.debug("Found {} upcoming events for user '{}'", upcomingEvents.size(), username);
        return eventMapper.toEventSummaryDtoList(upcomingEvents);
    }

    // ===============================
    // UPDATE OPERATIONS
    // ===============================

    /**
     * Updates an existing event for the authenticated user
     * 
     * @param eventId   Event ID to update
     * @param updateDto Event update data
     * @param username  Current user's username
     * @return Updated event details
     * @throws EventNotFoundException     if event not found
     * @throws EventAccessDeniedException if user has no access
     * @throws InvalidEventException      if validation fails
     * @throws EventConflictException     if conflicts exist
     */
    @Transactional
    public EventResponseDto updateEvent(Long eventId, EventUpdateDto updateDto, String username) {
        log.info("Updating event {} for user '{}'", eventId, username);

        // Get and validate access to event
        Event existingEvent = getEventByIdAndValidateAccess(eventId, username);
        User user = getUserByUsername(username);

        // Get user's other events for conflict validation
        List<Event> userEvents = eventRepository.findByUserId(user.getId());

        // Validate update with conflicts
        eventValidation.validateEventUpdateWithConflicts(updateDto, existingEvent, userEvents);

        // Update event
        Event updatedEvent = eventMapper.updateEventFromDto(existingEvent, updateDto);
        Event savedEvent = eventRepository.save(updatedEvent);

        log.info("Successfully updated event {} for user '{}'", eventId, username);
        return eventMapper.toEventResponseDto(savedEvent);
    }

    // ===============================
    // DELETE OPERATIONS
    // ===============================

    /**
     * Deletes an event for the authenticated user
     * 
     * @param eventId  Event ID to delete
     * @param username Current user's username
     * @return Success message
     * @throws EventNotFoundException     if event not found
     * @throws EventAccessDeniedException if user has no access
     * @throws InvalidEventException      if event cannot be deleted
     */
    @Transactional
    public MessageResponseDto deleteEvent(Long eventId, String username) {
        log.info("Deleting event {} for user '{}'", eventId, username);

        // Get and validate access to event
        Event event = getEventByIdAndValidateAccess(eventId, username);

        // Validate deletion
        eventValidation.validateEventDeletion(event);

        // Delete event
        eventRepository.delete(event);

        log.info("Successfully deleted event {} for user '{}'", eventId, username);
        return MessageResponseDto.success("Event deleted successfully");
    }

    // ===============================
    // STATISTICS OPERATIONS
    // ===============================

    /**
     * Gets event statistics for the authenticated user
     * 
     * @param username Current user's username
     * @return Event statistics
     */
    public EventStatisticsDto getUserEventStatistics(String username) {
        log.debug("Getting event statistics for user '{}'", username);

        User user = getUserByUsername(username);
        List<Event> userEvents = eventRepository.findByUserId(user.getId());

        EventStatisticsDto statistics = eventMapper.toEventStatisticsDto(userEvents);

        log.debug("Generated statistics for {} events for user '{}'",
                userEvents.size(), username);

        return statistics;
    }

    // ===============================
    // UTILITY OPERATIONS
    // ===============================

    /**
     * Checks if an event name is available for the user
     * 
     * @param eventName Event name to check
     * @param username  Current user's username
     * @return true if name is available
     */
    public boolean isEventNameAvailable(String eventName, String username) {
        log.debug("Checking availability of event name '{}' for user '{}'", eventName, username);

        User user = getUserByUsername(username);
        boolean exists = eventRepository.existsByEventNameAndUserId(eventName, user.getId());

        log.debug("Event name '{}' is {} for user '{}'",
                eventName, exists ? "taken" : "available", username);

        return !exists;
    }

    /**
     * Gets events that conflict with the given time range for the user
     * 
     * @param startDate Start date of the time range
     * @param endDate   End date of the time range
     * @param username  Current user's username
     * @return List of conflicting events
     */
    public List<EventSummaryDto> getConflictingEvents(LocalDateTime startDate,
            LocalDateTime endDate,
            String username) {
        log.debug("Finding conflicting events for user '{}' between {} and {}",
                username, startDate, endDate);

        User user = getUserByUsername(username);
        List<EventStatus> excludedStatuses = Arrays.asList(EventStatus.CANCELLED, EventStatus.COMPLETED);

        List<Event> conflictingEvents = eventRepository.findConflictingEvents(
                user.getId(), startDate, endDate, excludedStatuses);

        log.debug("Found {} conflicting events for user '{}'", conflictingEvents.size(), username);
        return eventMapper.toEventSummaryDtoList(conflictingEvents);
    }

    // ===============================
    // PRIVATE HELPER METHODS
    // ===============================

    /**
     * Gets user by username and throws exception if not found
     */
    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found with username: {}", username);
                    return new RuntimeException("User not found: " + username);
                });
    }

    /**
     * Gets event by ID and validates user access
     */
    private Event getEventByIdAndValidateAccess(Long eventId, String username) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> {
                    log.error("Event not found with ID: {}", eventId);
                    return new EventNotFoundException(eventId);
                });

        // Check if user has access to this event
        boolean hasAccess = event.getUsers().stream()
                .anyMatch(user -> user.getUsername().equals(username));

        if (!hasAccess) {
            log.error("User '{}' attempted to access event {} without permission", username, eventId);
            throw new EventAccessDeniedException(eventId, username);
        }

        return event;
    }

    /**
     * Checks if event matches search criteria
     */
    private boolean matchesSearchCriteria(Event event, EventSearchDto searchDto) {
        // Event name filter
        if (searchDto.getEventName() != null && !searchDto.getEventName().isEmpty()) {
            if (!event.getEventName().toLowerCase().contains(searchDto.getEventName().toLowerCase())) {
                return false;
            }
        }

        // Event type filter
        if (searchDto.getEventType() != null && !event.getEventType().equals(searchDto.getEventType())) {
            return false;
        }

        // Event status filter
        if (searchDto.getEventStatus() != null && !event.getEventStatus().equals(searchDto.getEventStatus())) {
            return false;
        }

        // Outdoor filter
        if (searchDto.getIsOutdoor() != null && !event.getIsOutdoor().equals(searchDto.getIsOutdoor())) {
            return false;
        }

        // Start date range filter
        if (searchDto.getStartDateFrom() != null && event.getStartDate().isBefore(searchDto.getStartDateFrom())) {
            return false;
        }
        if (searchDto.getStartDateTo() != null && event.getStartDate().isAfter(searchDto.getStartDateTo())) {
            return false;
        }

        // End date range filter (if event has end date)
        if (event.getEndDate() != null) {
            if (searchDto.getEndDateFrom() != null && event.getEndDate().isBefore(searchDto.getEndDateFrom())) {
                return false;
            }
            if (searchDto.getEndDateTo() != null && event.getEndDate().isAfter(searchDto.getEndDateTo())) {
                return false;
            }
        }

        return true;
    }
}
