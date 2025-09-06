package com.weather_found.weather_app.modules.event.exception;

/**
 * Exception thrown when an event is not found
 * 
 * @author Weather Found Team
 */
public class EventNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EventNotFoundException(String message) {
        super(message);
    }

    public EventNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventNotFoundException(Long eventId) {
        super("Event not found with ID: " + eventId);
    }

    public EventNotFoundException(String fieldName, Object fieldValue) {
        super("Event not found with " + fieldName + ": " + fieldValue);
    }
}
