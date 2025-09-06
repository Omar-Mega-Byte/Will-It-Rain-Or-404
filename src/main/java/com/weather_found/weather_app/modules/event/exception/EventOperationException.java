package com.weather_found.weather_app.modules.event.exception;

/**
 * Exception thrown when event operation fails due to external dependencies
 * (e.g., weather service unavailable, location service down)
 * 
 * @author Weather Found Team
 */
public class EventOperationException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public EventOperationException(String message) {
        super(message);
    }

    public EventOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public EventOperationException(String operation, String service, Throwable cause) {
        super("Event " + operation + " failed due to " + service + " service error", cause);
    }
}
