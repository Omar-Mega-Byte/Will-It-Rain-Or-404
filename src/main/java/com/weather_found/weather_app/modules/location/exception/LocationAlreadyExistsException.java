package com.weather_found.weather_app.modules.location.exception;

/**
 * Exception thrown when trying to create a location that already exists
 */
public class LocationAlreadyExistsException extends RuntimeException {

    public LocationAlreadyExistsException(String message) {
        super(message);
    }

    public LocationAlreadyExistsException(String name, String coordinates) {
        super("Location already exists: " + name + " at coordinates " + coordinates);
    }
}