package com.weather_found.weather_app.modules.event.exception;

/**
 * Utility class for creating standardized event-related exceptions
 * 
 * @author Weather Found Team
 */
public class EventExceptionFactory {

    private EventExceptionFactory() {
        // Utility class
    }

    // Event Not Found Exceptions
    public static EventNotFoundException eventNotFound(Long eventId) {
        return new EventNotFoundException("Event not found with ID: " + eventId);
    }

    public static EventNotFoundException eventNotFoundByName(String eventName) {
        return new EventNotFoundException("Event not found with name: " + eventName);
    }

    public static EventNotFoundException eventNotFoundForUser(Long eventId, String username) {
        return new EventNotFoundException("Event ID " + eventId + " not found for user: " + username);
    }

    // Invalid Event Exceptions
    public static InvalidEventException invalidEventName(String eventName) {
        return new InvalidEventException("Invalid event name: " + eventName);
    }

    public static InvalidEventException invalidEventDates(String reason) {
        return new InvalidEventException("Invalid event dates: " + reason);
    }

    public static InvalidEventException invalidEventStatus(String currentStatus, String newStatus) {
        return new InvalidEventException("Invalid status transition from " + currentStatus + " to " + newStatus);
    }

    public static InvalidEventException invalidEventType(String eventType, String reason) {
        return new InvalidEventException("Invalid event type " + eventType + ": " + reason);
    }

    public static InvalidEventException eventNotModifiable(String reason) {
        return new InvalidEventException("Event cannot be modified: " + reason);
    }

    // Event Conflict Exceptions
    public static EventConflictException eventNameExists(String eventName) {
        return new EventConflictException("Event name already exists: " + eventName);
    }

    public static EventConflictException eventScheduleConflict(String details) {
        return new EventConflictException("Schedule conflict: " + details);
    }

    public static EventConflictException eventCapacityExceeded(int current, int maximum) {
        return new EventConflictException("Event capacity exceeded: " + current + "/" + maximum);
    }

    // Event Access Denied Exceptions
    public static EventAccessDeniedException accessDenied(Long eventId, String username) {
        return new EventAccessDeniedException("Access denied to event ID " + eventId + " for user: " + username);
    }

    public static EventAccessDeniedException insufficientPermissions(String operation, String username) {
        return new EventAccessDeniedException("Insufficient permissions for " + operation + " by user: " + username);
    }

    // Event Operation Exceptions
    public static EventOperationException operationFailed(String operation, String reason) {
        return new EventOperationException("Event " + operation + " failed: " + reason);
    }

    public static EventOperationException serviceUnavailable(String serviceName, Throwable cause) {
        return new EventOperationException("Event operation failed due to " + serviceName + " service unavailability",
                cause);
    }
}
