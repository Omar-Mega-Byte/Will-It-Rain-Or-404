package com.weather_found.weather_app.modules.event.validation;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.weather_found.weather_app.modules.event.dto.request.EventCreateDto;
import com.weather_found.weather_app.modules.event.dto.request.EventUpdateDto;
import com.weather_found.weather_app.modules.event.exception.EventConflictException;
import com.weather_found.weather_app.modules.event.exception.InvalidEventException;
import com.weather_found.weather_app.modules.event.model.Event;
import com.weather_found.weather_app.modules.event.model.enums.EventStatus;
import com.weather_found.weather_app.modules.event.model.enums.EventType;

import lombok.extern.slf4j.Slf4j;

/**
 * ULTIMATE Event Validation Service
 * Handles all business logic validations for event operations
 * 
 * @author Weather Found Team
 * @version 1.0
 */
@Slf4j
@Component
public class EventValidation {

    // Constants for validation rules
    private static final int MIN_EVENT_DURATION_MINUTES = 30;
    private static final int MAX_EVENT_DURATION_DAYS = 365;
    private static final int MIN_ADVANCE_BOOKING_HOURS = 1;
    private static final int MAX_ADVANCE_BOOKING_YEARS = 2;
    private static final int MAX_PARTICIPANTS_PER_EVENT = 10000;
    private static final int MIN_EVENT_NAME_LENGTH = 3;
    private static final int MAX_EVENT_NAME_LENGTH = 255;
    private static final int MIN_DESCRIPTION_LENGTH = 10;
    private static final int MAX_DESCRIPTION_LENGTH = 1000;

    // Forbidden words in event names/descriptions
    private static final Set<String> FORBIDDEN_WORDS = Set.of(
            "spam", "fake", "scam", "illegal", "prohibited", "banned");

    // Event types that must be outdoor
    private static final Set<EventType> MUST_BE_OUTDOOR = Set.of(
            EventType.SPORTS, EventType.FESTIVAL);

    // Event types that cannot be outdoor
    private static final Set<EventType> MUST_BE_INDOOR = Set.of(
            EventType.CONFERENCE, EventType.MEETING);

    /**
     * Validates event creation request
     * 
     * @param createDto Event creation data
     * @throws InvalidEventException  if validation fails
     * @throws EventConflictException if conflicts with existing events
     */
    public void validateEventCreation(EventCreateDto createDto) {
        log.debug("Validating event creation: {}", createDto.getEventName());

        // Basic field validations
        validateEventName(createDto.getEventName());
        validateEventDescription(createDto.getEventDescription());
        validateEventType(createDto.getEventType());
        validateEventDates(createDto.getStartDate(), createDto.getEndDate());
        validateOutdoorFlag(createDto.getIsOutdoor(), createDto.getEventType());
        validateEventStatus(createDto.getEventStatus());
        validateParticipants(createDto.getUserIds());

        // Business logic validations
        validateEventTiming(createDto.getStartDate(), createDto.getEndDate());
        validateEventScheduling(createDto.getStartDate());

        log.debug("Event creation validation passed for: {}", createDto.getEventName());
    }

    /**
     * Validates event creation with conflict checking
     * 
     * @param createDto      Event creation data
     * @param existingEvents List of user's existing events
     * @throws InvalidEventException  if validation fails
     * @throws EventConflictException if conflicts with existing events
     */
    public void validateEventCreationWithConflicts(EventCreateDto createDto, List<Event> existingEvents) {
        validateEventCreation(createDto);
        validateUniqueEventName(createDto.getEventName(), existingEvents);
        validateEventConflicts(createDto.getStartDate(), createDto.getEndDate(), existingEvents);
    }

    /**
     * Validates event update request
     * 
     * @param updateDto     Event update data
     * @param existingEvent Current event state
     * @throws InvalidEventException if validation fails
     */
    public void validateEventUpdate(EventUpdateDto updateDto, Event existingEvent) {
        log.debug("Validating event update for event: {}", existingEvent.getEventName());

        // Check if event can be updated based on current status
        validateEventModifiable(existingEvent);

        // Validate individual fields if they are being updated
        if (updateDto.getEventName() != null) {
            validateEventName(updateDto.getEventName());
        }

        if (updateDto.getEventDescription() != null) {
            validateEventDescription(updateDto.getEventDescription());
        }

        if (updateDto.getEventType() != null) {
            validateEventType(updateDto.getEventType());
            validateEventTypeChange(existingEvent.getEventType(), updateDto.getEventType(),
                    existingEvent.getEventStatus());
        }

        if (updateDto.getIsOutdoor() != null && updateDto.getEventType() != null) {
            validateOutdoorFlag(updateDto.getIsOutdoor(), updateDto.getEventType());
        } else if (updateDto.getIsOutdoor() != null) {
            validateOutdoorFlag(updateDto.getIsOutdoor(), existingEvent.getEventType());
        }

        if (updateDto.getEventStatus() != null) {
            validateEventStatusTransition(existingEvent.getEventStatus(), updateDto.getEventStatus());
        }

        if (updateDto.getUserIds() != null) {
            validateParticipants(updateDto.getUserIds());
        }

        // Validate dates if either is being updated
        LocalDateTime newStartDate = updateDto.getStartDate() != null ? updateDto.getStartDate()
                : existingEvent.getStartDate();
        LocalDateTime newEndDate = updateDto.getEndDate() != null ? updateDto.getEndDate() : existingEvent.getEndDate();

        if (updateDto.getStartDate() != null || updateDto.getEndDate() != null) {
            validateEventDates(newStartDate, newEndDate);
            validateEventTiming(newStartDate, newEndDate);

            // Only validate scheduling if start date is being changed and event hasn't
            // started
            if (updateDto.getStartDate() != null && existingEvent.getEventStatus() == EventStatus.SCHEDULED) {
                validateEventScheduling(newStartDate);
            }
        }

        log.debug("Event update validation passed for event: {}", existingEvent.getEventName());
    }

    /**
     * Validates event update with conflict checking
     * 
     * @param updateDto     Event update data
     * @param existingEvent Current event state
     * @param userEvents    List of user's other events
     * @throws InvalidEventException  if validation fails
     * @throws EventConflictException if conflicts with existing events
     */
    public void validateEventUpdateWithConflicts(EventUpdateDto updateDto, Event existingEvent,
            List<Event> userEvents) {
        validateEventUpdate(updateDto, existingEvent);

        if (updateDto.getEventName() != null) {
            validateUniqueEventNameForUpdate(updateDto.getEventName(), userEvents, existingEvent);
        }

        // Validate dates conflicts if either is being updated
        LocalDateTime newStartDate = updateDto.getStartDate() != null ? updateDto.getStartDate()
                : existingEvent.getStartDate();
        LocalDateTime newEndDate = updateDto.getEndDate() != null ? updateDto.getEndDate() : existingEvent.getEndDate();

        if (updateDto.getStartDate() != null || updateDto.getEndDate() != null) {
            validateEventConflictsForUpdate(newStartDate, newEndDate, userEvents, existingEvent);
        }
    }

    /**
     * Validates event deletion
     * 
     * @param event Event to be deleted
     * @throws InvalidEventException if event cannot be deleted
     */
    public void validateEventDeletion(Event event) {
        log.debug("Validating event deletion for: {}", event.getEventName());

        if (event.getEventStatus() == EventStatus.IN_PROGRESS) {
            throw new InvalidEventException("Cannot delete an event that is currently in progress");
        }

        if (event.getEventStatus() == EventStatus.COMPLETED) {
            throw new InvalidEventException("Cannot delete a completed event for audit purposes");
        }

        log.debug("Event deletion validation passed for: {}", event.getEventName());
    }

    /**
     * Validates event name uniqueness
     * 
     * @param eventName      Name to check
     * @param existingEvents List of existing events
     * @throws EventConflictException if name already exists
     */
    public void validateUniqueEventName(String eventName, List<Event> existingEvents) {
        boolean nameExists = existingEvents.stream()
                .anyMatch(event -> event.getEventName().equalsIgnoreCase(eventName.trim()));

        if (nameExists) {
            throw new EventConflictException("An event with the name '" + eventName + "' already exists");
        }
    }

    /**
     * Validates event conflicts for new events
     * 
     * @param startDate      Event start date
     * @param endDate        Event end date
     * @param existingEvents List of existing events
     * @throws EventConflictException if schedule conflicts exist
     */
    public void validateEventConflicts(LocalDateTime startDate, LocalDateTime endDate, List<Event> existingEvents) {
        LocalDateTime effectiveEndDate = endDate != null ? endDate : startDate.plusHours(1);

        boolean hasConflict = existingEvents.stream()
                .filter(event -> event.getEventStatus() != EventStatus.CANCELLED
                        && event.getEventStatus() != EventStatus.COMPLETED)
                .anyMatch(event -> {
                    LocalDateTime existingStart = event.getStartDate();
                    LocalDateTime existingEnd = event.getEndDate() != null ? event.getEndDate()
                            : existingStart.plusHours(1);

                    return isTimeOverlap(startDate, effectiveEndDate, existingStart, existingEnd);
                });

        if (hasConflict) {
            throw new EventConflictException("Event conflicts with an existing scheduled event");
        }
    }

    // ===============================
    // PRIVATE VALIDATION METHODS
    // ===============================

    private void validateEventName(String eventName) {
        if (eventName == null || eventName.trim().isEmpty()) {
            throw new InvalidEventException("Event name cannot be empty");
        }

        String trimmedName = eventName.trim();

        if (trimmedName.length() < MIN_EVENT_NAME_LENGTH) {
            throw new InvalidEventException(
                    "Event name must be at least " + MIN_EVENT_NAME_LENGTH + " characters long");
        }

        if (trimmedName.length() > MAX_EVENT_NAME_LENGTH) {
            throw new InvalidEventException("Event name must not exceed " + MAX_EVENT_NAME_LENGTH + " characters");
        }

        // Check for forbidden words
        String lowerCaseName = trimmedName.toLowerCase();
        for (String forbiddenWord : FORBIDDEN_WORDS) {
            if (lowerCaseName.contains(forbiddenWord)) {
                throw new InvalidEventException("Event name contains prohibited content: " + forbiddenWord);
            }
        }

        // Check for excessive special characters
        long specialCharCount = trimmedName.chars()
                .filter(ch -> !Character.isLetterOrDigit(ch) && !Character.isWhitespace(ch))
                .count();

        if (specialCharCount > trimmedName.length() * 0.3) {
            throw new InvalidEventException("Event name contains too many special characters");
        }
    }

    private void validateEventDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new InvalidEventException("Event description cannot be empty");
        }

        String trimmedDesc = description.trim();

        if (trimmedDesc.length() < MIN_DESCRIPTION_LENGTH) {
            throw new InvalidEventException(
                    "Event description must be at least " + MIN_DESCRIPTION_LENGTH + " characters long");
        }

        if (trimmedDesc.length() > MAX_DESCRIPTION_LENGTH) {
            throw new InvalidEventException(
                    "Event description must not exceed " + MAX_DESCRIPTION_LENGTH + " characters");
        }

        // Check for forbidden words
        String lowerCaseDesc = trimmedDesc.toLowerCase();
        for (String forbiddenWord : FORBIDDEN_WORDS) {
            if (lowerCaseDesc.contains(forbiddenWord)) {
                throw new InvalidEventException("Event description contains prohibited content: " + forbiddenWord);
            }
        }
    }

    private void validateEventType(EventType eventType) {
        if (eventType == null) {
            throw new InvalidEventException("Event type cannot be null");
        }
    }

    private void validateEventDates(LocalDateTime startDate, LocalDateTime endDate) {
        if (startDate == null) {
            throw new InvalidEventException("Event start date cannot be null");
        }

        if (endDate != null && endDate.isBefore(startDate)) {
            throw new InvalidEventException("Event end date cannot be before start date");
        }

        if (endDate != null && endDate.equals(startDate)) {
            throw new InvalidEventException("Event end date cannot be the same as start date");
        }
    }

    private void validateEventTiming(LocalDateTime startDate, LocalDateTime endDate) {
        // Validate minimum duration
        if (endDate != null) {
            long durationMinutes = ChronoUnit.MINUTES.between(startDate, endDate);
            if (durationMinutes < MIN_EVENT_DURATION_MINUTES) {
                throw new InvalidEventException(
                        "Event duration must be at least " + MIN_EVENT_DURATION_MINUTES + " minutes");
            }

            long durationDays = ChronoUnit.DAYS.between(startDate, endDate);
            if (durationDays > MAX_EVENT_DURATION_DAYS) {
                throw new InvalidEventException("Event duration cannot exceed " + MAX_EVENT_DURATION_DAYS + " days");
            }
        }

        // Validate reasonable hours (events between 5 AM and 11 PM)
        int startHour = startDate.getHour();
        if (startHour < 5 || startHour > 23) {
            log.warn("Event scheduled outside normal hours: {}", startDate);
        }
    }

    private void validateEventScheduling(LocalDateTime startDate) {
        LocalDateTime now = LocalDateTime.now();

        // Check minimum advance booking time
        long hoursUntilEvent = ChronoUnit.HOURS.between(now, startDate);
        if (hoursUntilEvent < MIN_ADVANCE_BOOKING_HOURS) {
            throw new InvalidEventException(
                    "Event must be scheduled at least " + MIN_ADVANCE_BOOKING_HOURS + " hour(s) in advance");
        }

        // Check maximum advance booking time
        long yearsUntilEvent = ChronoUnit.YEARS.between(now, startDate);
        if (yearsUntilEvent > MAX_ADVANCE_BOOKING_YEARS) {
            throw new InvalidEventException(
                    "Event cannot be scheduled more than " + MAX_ADVANCE_BOOKING_YEARS + " years in advance");
        }
    }

    private void validateOutdoorFlag(Boolean isOutdoor, EventType eventType) {
        if (isOutdoor == null) {
            throw new InvalidEventException("Outdoor flag cannot be null");
        }

        if (MUST_BE_OUTDOOR.contains(eventType) && !isOutdoor) {
            throw new InvalidEventException("Event type " + eventType + " must be outdoor");
        }

        if (MUST_BE_INDOOR.contains(eventType) && isOutdoor) {
            throw new InvalidEventException("Event type " + eventType + " must be indoor");
        }
    }

    private void validateEventStatus(EventStatus eventStatus) {
        if (eventStatus == null) {
            // Default status is SCHEDULED, which is valid
            return;
        }

        // New events can only be created with SCHEDULED status
        if (eventStatus != EventStatus.SCHEDULED) {
            throw new InvalidEventException("New events can only be created with SCHEDULED status");
        }
    }

    private void validateEventStatusTransition(EventStatus currentStatus, EventStatus newStatus) {
        if (currentStatus == newStatus) {
            throw new InvalidEventException("Event is already in " + currentStatus + " status");
        }

        // Define valid status transitions
        boolean validTransition = switch (currentStatus) {
            case SCHEDULED -> newStatus == EventStatus.IN_PROGRESS || newStatus == EventStatus.CANCELLED;
            case IN_PROGRESS -> newStatus == EventStatus.COMPLETED || newStatus == EventStatus.CANCELLED;
            case COMPLETED -> false; // No transitions allowed from completed
            case CANCELLED -> newStatus == EventStatus.SCHEDULED; // Can reschedule cancelled events
        };

        if (!validTransition) {
            throw new InvalidEventException("Invalid status transition from " + currentStatus + " to " + newStatus);
        }
    }

    private void validateEventTypeChange(EventType currentType, EventType newType, EventStatus eventStatus) {
        if (eventStatus == EventStatus.IN_PROGRESS || eventStatus == EventStatus.COMPLETED) {
            throw new InvalidEventException("Cannot change event type for events that are in progress or completed");
        }

        // Some event types cannot be changed to others due to fundamental differences
        if (currentType == EventType.CONFERENCE && newType == EventType.SPORTS) {
            throw new InvalidEventException("Cannot change conference to sports event - fundamental incompatibility");
        }
    }

    private void validateParticipants(List<Long> userIds) {
        if (userIds != null && userIds.size() > MAX_PARTICIPANTS_PER_EVENT) {
            throw new InvalidEventException(
                    "Event cannot have more than " + MAX_PARTICIPANTS_PER_EVENT + " participants");
        }
    }

    private void validateUniqueEventNameForUpdate(String eventName, List<Event> userEvents, Event currentEvent) {
        boolean nameExists = userEvents.stream()
                .filter(event -> !event.getId().equals(currentEvent.getId()))
                .anyMatch(event -> event.getEventName().equalsIgnoreCase(eventName.trim()));

        if (nameExists) {
            throw new EventConflictException("An event with the name '" + eventName + "' already exists");
        }
    }

    private void validateEventConflictsForUpdate(LocalDateTime startDate, LocalDateTime endDate,
            List<Event> userEvents, Event currentEvent) {
        LocalDateTime effectiveEndDate = endDate != null ? endDate : startDate.plusHours(1);

        boolean hasConflict = userEvents.stream()
                .filter(event -> !event.getId().equals(currentEvent.getId()))
                .filter(event -> event.getEventStatus() != EventStatus.CANCELLED
                        && event.getEventStatus() != EventStatus.COMPLETED)
                .anyMatch(event -> {
                    LocalDateTime existingStart = event.getStartDate();
                    LocalDateTime existingEnd = event.getEndDate() != null ? event.getEndDate()
                            : existingStart.plusHours(1);

                    return isTimeOverlap(startDate, effectiveEndDate, existingStart, existingEnd);
                });

        if (hasConflict) {
            throw new EventConflictException("Event conflicts with an existing scheduled event");
        }
    }

    private void validateEventModifiable(Event event) {
        if (event.getEventStatus() == EventStatus.COMPLETED) {
            throw new InvalidEventException("Cannot modify a completed event");
        }

        // Allow modifications to in-progress events with limitations
        if (event.getEventStatus() == EventStatus.IN_PROGRESS) {
            log.warn("Modifying in-progress event: {}", event.getEventName());
        }
    }

    private boolean isTimeOverlap(LocalDateTime start1, LocalDateTime end1, LocalDateTime start2, LocalDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}
