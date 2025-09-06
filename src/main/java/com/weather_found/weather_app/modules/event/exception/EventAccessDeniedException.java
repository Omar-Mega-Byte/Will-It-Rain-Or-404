package com.weather_found.weather_app.modules.event.exception;

/**
 * Exception thrown when user lacks permission to access or modify an event
 * 
 * @author Weather Found Team
 */
public class EventAccessDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EventAccessDeniedException(String message) {
        super(message);
    }

    public EventAccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventAccessDeniedException(Long eventId, String username) {
        super("User '" + username + "' does not have access to event ID: " + eventId);
    }
}
