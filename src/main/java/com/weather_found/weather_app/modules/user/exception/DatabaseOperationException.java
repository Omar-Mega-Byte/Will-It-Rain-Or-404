package com.weather_found.weather_app.modules.user.exception;

/**
 * Exception thrown when database operations fail
 */
public class DatabaseOperationException extends RuntimeException {

    public DatabaseOperationException(String message) {
        super(message);
    }

    public DatabaseOperationException(String message, Throwable cause) {
        super(message, cause);
    }
}
