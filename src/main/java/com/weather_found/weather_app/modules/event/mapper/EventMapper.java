package com.weather_found.weather_app.modules.event.mapper;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import com.weather_found.weather_app.modules.event.dto.request.EventCreateDto;
import com.weather_found.weather_app.modules.event.dto.request.EventUpdateDto;
import com.weather_found.weather_app.modules.event.dto.response.EventPageResponseDto;
import com.weather_found.weather_app.modules.event.dto.response.EventResponseDto;
import com.weather_found.weather_app.modules.event.dto.response.EventStatisticsDto;
import com.weather_found.weather_app.modules.event.dto.response.EventSummaryDto;
import com.weather_found.weather_app.modules.event.model.Event;
import com.weather_found.weather_app.modules.event.model.enums.EventStatus;
import com.weather_found.weather_app.modules.event.model.enums.EventType;
import com.weather_found.weather_app.modules.user.model.User;

/**
 * ULTIMATE Event Mapper
 * Handles all conversions between Event entities and DTOs
 * 
 * @author Weather Found Team
 * @version 1.0
 */
@Component
public class EventMapper {

    // ===============================
    // ENTITY TO DTO MAPPINGS
    // ===============================

    /**
     * Converts Event entity to EventResponseDto
     * 
     * @param event The event entity
     * @return EventResponseDto with complete event details
     */
    public EventResponseDto toEventResponseDto(Event event) {
        if (event == null) {
            return null;
        }

        EventResponseDto dto = new EventResponseDto();
        dto.setId(event.getId());
        dto.setEventName(event.getEventName());
        dto.setEventDescription(event.getEventDescription());
        dto.setEventType(event.getEventType());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setIsOutdoor(event.getIsOutdoor());
        dto.setEventStatus(event.getEventStatus());
        dto.setCreatedAt(convertToLocalDateTime(event.getCreatedAt()));
        dto.setUpdatedAt(convertToLocalDateTime(event.getUpdatedAt()));

        // Map users to EventUserDto
        if (event.getUsers() != null) {
            List<EventResponseDto.EventUserDto> userDtos = event.getUsers().stream()
                    .map(this::toEventUserDto)
                    .collect(Collectors.toList());
            dto.setUsers(userDtos);
        } else {
            dto.setUsers(new ArrayList<>());
        }

        return dto;
    }

    /**
     * Converts Event entity to EventSummaryDto
     * 
     * @param event The event entity
     * @return EventSummaryDto with essential event information
     */
    public EventSummaryDto toEventSummaryDto(Event event) {
        if (event == null) {
            return null;
        }

        EventSummaryDto dto = new EventSummaryDto();
        dto.setId(event.getId());
        dto.setEventName(event.getEventName());
        dto.setEventType(event.getEventType());
        dto.setStartDate(event.getStartDate());
        dto.setEndDate(event.getEndDate());
        dto.setIsOutdoor(event.getIsOutdoor());
        dto.setEventStatus(event.getEventStatus());
        dto.setParticipantCount(event.getUsers() != null ? event.getUsers().size() : 0);
        dto.setCreatedAt(convertToLocalDateTime(event.getCreatedAt()));

        return dto;
    }

    /**
     * Converts list of Events to list of EventSummaryDto
     * 
     * @param events List of event entities
     * @return List of EventSummaryDto
     */
    public List<EventSummaryDto> toEventSummaryDtoList(List<Event> events) {
        if (events == null) {
            return new ArrayList<>();
        }

        return events.stream()
                .map(this::toEventSummaryDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts list of Events to list of EventResponseDto
     * 
     * @param events List of event entities
     * @return List of EventResponseDto
     */
    public List<EventResponseDto> toEventResponseDtoList(List<Event> events) {
        if (events == null) {
            return new ArrayList<>();
        }

        return events.stream()
                .map(this::toEventResponseDto)
                .collect(Collectors.toList());
    }

    /**
     * Converts Page<Event> to EventPageResponseDto
     * 
     * @param eventPage Page of events
     * @return EventPageResponseDto with pagination information
     */
    public EventPageResponseDto toEventPageResponseDto(Page<Event> eventPage) {
        if (eventPage == null) {
            return new EventPageResponseDto();
        }

        EventPageResponseDto dto = new EventPageResponseDto();
        dto.setEvents(toEventSummaryDtoList(eventPage.getContent()));
        dto.setCurrentPage(eventPage.getNumber());
        dto.setTotalPages(eventPage.getTotalPages());
        dto.setTotalElements(eventPage.getTotalElements());
        dto.setSize(eventPage.getSize());
        dto.setHasNext(eventPage.hasNext());
        dto.setHasPrevious(eventPage.hasPrevious());
        dto.setIsFirst(eventPage.isFirst());
        dto.setIsLast(eventPage.isLast());

        return dto;
    }

    /**
     * Creates EventStatisticsDto from event data
     * 
     * @param events List of events for statistics
     * @return EventStatisticsDto with calculated statistics
     */
    public EventStatisticsDto toEventStatisticsDto(List<Event> events) {
        if (events == null) {
            events = new ArrayList<>();
        }

        EventStatisticsDto dto = new EventStatisticsDto();

        // Basic counts
        dto.setTotalEvents((long) events.size());
        dto.setUpcomingEvents(events.stream()
                .filter(e -> e.getEventStatus() == EventStatus.SCHEDULED)
                .count());
        dto.setActiveEvents(events.stream()
                .filter(e -> e.getEventStatus() == EventStatus.IN_PROGRESS)
                .count());
        dto.setCompletedEvents(events.stream()
                .filter(e -> e.getEventStatus() == EventStatus.COMPLETED)
                .count());
        dto.setCancelledEvents(events.stream()
                .filter(e -> e.getEventStatus() == EventStatus.CANCELLED)
                .count());
        dto.setOutdoorEvents(events.stream()
                .filter(e -> Boolean.TRUE.equals(e.getIsOutdoor()))
                .count());
        dto.setIndoorEvents(events.stream()
                .filter(e -> Boolean.FALSE.equals(e.getIsOutdoor()))
                .count());

        // Group by event type
        dto.setEventsByType(events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getEventType().toString(),
                        Collectors.counting())));

        // Group by event status
        dto.setEventsByStatus(events.stream()
                .collect(Collectors.groupingBy(
                        e -> e.getEventStatus().toString(),
                        Collectors.counting())));

        return dto;
    }

    // ===============================
    // DTO TO ENTITY MAPPINGS
    // ===============================

    /**
     * Converts EventCreateDto to Event entity
     * 
     * @param createDto The creation DTO
     * @return New Event entity
     */
    public Event toEvent(EventCreateDto createDto) {
        if (createDto == null) {
            return null;
        }

        Event event = new Event();
        event.setEventName(createDto.getEventName());
        event.setEventDescription(createDto.getEventDescription());
        event.setEventType(createDto.getEventType());
        event.setStartDate(createDto.getStartDate());
        event.setEndDate(createDto.getEndDate());
        event.setIsOutdoor(createDto.getIsOutdoor());
        event.setEventStatus(createDto.getEventStatus() != null ? createDto.getEventStatus() : EventStatus.SCHEDULED);
        event.setUsers(new ArrayList<>());

        return event;
    }

    /**
     * Updates Event entity with EventUpdateDto data
     * 
     * @param event     The existing event entity
     * @param updateDto The update DTO
     * @return Updated event entity
     */
    public Event updateEventFromDto(Event event, EventUpdateDto updateDto) {
        if (event == null || updateDto == null) {
            return event;
        }

        // Update fields only if they are not null in the DTO
        if (updateDto.getEventName() != null) {
            event.setEventName(updateDto.getEventName());
        }

        if (updateDto.getEventDescription() != null) {
            event.setEventDescription(updateDto.getEventDescription());
        }

        if (updateDto.getEventType() != null) {
            event.setEventType(updateDto.getEventType());
        }

        if (updateDto.getStartDate() != null) {
            event.setStartDate(updateDto.getStartDate());
        }

        if (updateDto.getEndDate() != null) {
            event.setEndDate(updateDto.getEndDate());
        }

        if (updateDto.getIsOutdoor() != null) {
            event.setIsOutdoor(updateDto.getIsOutdoor());
        }

        if (updateDto.getEventStatus() != null) {
            event.setEventStatus(updateDto.getEventStatus());
        }

        return event;
    }

    // ===============================
    // HELPER MAPPINGS
    // ===============================

    /**
     * Converts User entity to EventUserDto
     * 
     * @param user The user entity
     * @return EventUserDto with user information
     */
    private EventResponseDto.EventUserDto toEventUserDto(User user) {
        if (user == null) {
            return null;
        }

        EventResponseDto.EventUserDto dto = new EventResponseDto.EventUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());

        return dto;
    }

    /**
     * Converts Instant to LocalDateTime
     * 
     * @param instant The instant to convert
     * @return LocalDateTime equivalent
     */
    private java.time.LocalDateTime convertToLocalDateTime(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(java.time.ZoneId.systemDefault()).toLocalDateTime();
    }

    // ===============================
    // UTILITY MAPPING METHODS
    // ===============================

    /**
     * Creates a new Event entity with default values
     * 
     * @return New Event entity with defaults
     */
    public Event createDefaultEvent() {
        Event event = new Event();
        event.setEventStatus(EventStatus.SCHEDULED);
        event.setIsOutdoor(true);
        event.setUsers(new ArrayList<>());
        return event;
    }

    /**
     * Creates a copy of an Event entity
     * 
     * @param original The original event
     * @return Copied event entity
     */
    public Event copyEvent(Event original) {
        if (original == null) {
            return null;
        }

        Event copy = new Event();
        copy.setEventName(original.getEventName());
        copy.setEventDescription(original.getEventDescription());
        copy.setEventType(original.getEventType());
        copy.setStartDate(original.getStartDate());
        copy.setEndDate(original.getEndDate());
        copy.setIsOutdoor(original.getIsOutdoor());
        copy.setEventStatus(original.getEventStatus());
        copy.setUsers(original.getUsers() != null ? new ArrayList<>(original.getUsers()) : new ArrayList<>());

        return copy;
    }

    /**
     * Checks if two events have the same essential data
     * 
     * @param event1 First event
     * @param event2 Second event
     * @return true if events have same essential data
     */
    public boolean haveSameEssentialData(Event event1, Event event2) {
        if (event1 == null || event2 == null) {
            return event1 == event2;
        }

        return event1.getEventName().equals(event2.getEventName()) &&
                event1.getEventType().equals(event2.getEventType()) &&
                event1.getStartDate().equals(event2.getStartDate()) &&
                java.util.Objects.equals(event1.getEndDate(), event2.getEndDate()) &&
                event1.getIsOutdoor().equals(event2.getIsOutdoor());
    }

    /**
     * Gets display name for event type
     * 
     * @param eventType The event type
     * @return Human-readable event type name
     */
    public String getEventTypeDisplayName(EventType eventType) {
        if (eventType == null) {
            return "Unknown";
        }

        return switch (eventType) {
            case CONCERT -> "Concert";
            case CONFERENCE -> "Conference";
            case MEETING -> "Meeting";
            case WORKSHOP -> "Workshop";
            case SPORTS -> "Sports Event";
            case FESTIVAL -> "Festival";
            case OTHER -> "Other";
        };
    }

    /**
     * Gets display name for event status
     * 
     * @param eventStatus The event status
     * @return Human-readable event status name
     */
    public String getEventStatusDisplayName(EventStatus eventStatus) {
        if (eventStatus == null) {
            return "Unknown";
        }

        return switch (eventStatus) {
            case SCHEDULED -> "Scheduled";
            case IN_PROGRESS -> "In Progress";
            case COMPLETED -> "Completed";
            case CANCELLED -> "Cancelled";
        };
    }

    /**
     * Gets CSS class for event status (for UI styling)
     * 
     * @param eventStatus The event status
     * @return CSS class name for status
     */
    public String getEventStatusCssClass(EventStatus eventStatus) {
        if (eventStatus == null) {
            return "status-unknown";
        }

        return switch (eventStatus) {
            case SCHEDULED -> "status-scheduled";
            case IN_PROGRESS -> "status-in-progress";
            case COMPLETED -> "status-completed";
            case CANCELLED -> "status-cancelled";
        };
    }

    /**
     * Determines if an event can be modified based on its status
     * 
     * @param event The event to check
     * @return true if event can be modified
     */
    public boolean canEventBeModified(Event event) {
        if (event == null) {
            return false;
        }

        return event.getEventStatus() == EventStatus.SCHEDULED ||
                event.getEventStatus() == EventStatus.CANCELLED;
    }

    /**
     * Determines if an event can be deleted based on its status
     * 
     * @param event The event to check
     * @return true if event can be deleted
     */
    public boolean canEventBeDeleted(Event event) {
        if (event == null) {
            return false;
        }

        return event.getEventStatus() == EventStatus.SCHEDULED ||
                event.getEventStatus() == EventStatus.CANCELLED;
    }
}
