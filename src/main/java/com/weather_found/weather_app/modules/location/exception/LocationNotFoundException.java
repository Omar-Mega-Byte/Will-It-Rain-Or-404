package com.weather_found.weather_app.modules.location.exception;

/**
 * Exception thrown when a location is not found
 */
public class LocationNotFoundException extends RuntimeException {

    public LocationNotFoundException(String message) {
        super(message);
    }

    public LocationNotFoundException(Long id) {
        super("Location not found with ID: " + id);
    }

    public LocationNotFoundException(String field, String value) {
        super("Location not found with " + field + ": " + value);
    }
}