package com.weather_found.weather_app.modules.event.exception;

/**
 * Exception thrown when there are conflicts with existing events
 * (e.g., scheduling conflicts, duplicate names)
 * 
 * @author Weather Found Team
 */
public class EventConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EventConflictException(String message) {
        super(message);
    }

    public EventConflictException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventConflictException(String conflictType, String details) {
        super(conflictType + " conflict: " + details);
    }
}
