package com.weather_found.weather_app.modules.event.exception;

/**
 * Exception thrown when event data or operations are invalid
 * 
 * @author Weather Found Team
 */
public class InvalidEventException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidEventException(String message) {
        super(message);
    }

    public InvalidEventException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidEventException(String field, String value, String reason) {
        super("Invalid " + field + " '" + value + "': " + reason);
    }
}
