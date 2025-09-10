package com.weather_found.weather_app.modules.weather.validation;

import com.weather_found.weather_app.modules.weather.model.Location;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Validation utility for weather module inputs and data
 */
@Component
@Slf4j
public class WeatherValidationService {

    // Validation patterns
    private static final Pattern LOCATION_NAME_PATTERN = Pattern.compile("^[a-zA-Z0-9\\s\\-,.'()]+$");
    private static final Pattern COUNTRY_CODE_PATTERN = Pattern.compile("^[A-Z]{2}$");
    private static final Pattern TIMEZONE_PATTERN = Pattern.compile("^[A-Za-z_]+/[A-Za-z_]+$");
    private static final Pattern ALERT_TYPE_PATTERN = Pattern.compile("^[A-Z_]+$");
    private static final Pattern SEVERITY_PATTERN = Pattern.compile("^(LOW|MEDIUM|HIGH|CRITICAL)$");

    // Geographic bounds
    private static final BigDecimal MIN_LATITUDE = new BigDecimal("-90.0");
    private static final BigDecimal MAX_LATITUDE = new BigDecimal("90.0");
    private static final BigDecimal MIN_LONGITUDE = new BigDecimal("-180.0");
    private static final BigDecimal MAX_LONGITUDE = new BigDecimal("180.0");

    // Temperature bounds (Celsius)
    private static final BigDecimal MIN_TEMPERATURE = new BigDecimal("-100.0");
    private static final BigDecimal MAX_TEMPERATURE = new BigDecimal("70.0");

    // Other bounds
    private static final BigDecimal MIN_HUMIDITY = new BigDecimal("0.0");
    private static final BigDecimal MAX_HUMIDITY = new BigDecimal("100.0");
    private static final BigDecimal MIN_PRESSURE = new BigDecimal("800.0");
    private static final BigDecimal MAX_PRESSURE = new BigDecimal("1200.0");

    /**
     * Validation result container
     */
    public static class ValidationResult {
        private final boolean valid;
        private final List<String> errors;

        public ValidationResult(boolean valid, List<String> errors) {
            this.valid = valid;
            this.errors = errors;
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public String getErrorMessage() {
            return String.join(", ", errors);
        }
    }

    /**
     * Validate location data
     */
    public ValidationResult validateLocation(Location location) {
        List<String> errors = new ArrayList<>();

        if (location == null) {
            errors.add("Location cannot be null");
            return new ValidationResult(false, errors);
        }

        // Validate name
        if (location.getName() == null || location.getName().trim().isEmpty()) {
            errors.add("Location name is required");
        } else if (location.getName().length() > 100) {
            errors.add("Location name must be less than 100 characters");
        } else if (!LOCATION_NAME_PATTERN.matcher(location.getName()).matches()) {
            errors.add("Location name contains invalid characters");
        }

        // Validate coordinates
        if (location.getLatitude() == null) {
            errors.add("Latitude is required");
        } else if (!isValidLatitude(location.getLatitude())) {
            errors.add("Latitude must be between -90 and 90 degrees");
        }

        if (location.getLongitude() == null) {
            errors.add("Longitude is required");
        } else if (!isValidLongitude(location.getLongitude())) {
            errors.add("Longitude must be between -180 and 180 degrees");
        }

        // Validate country
        if (location.getCountry() == null || location.getCountry().trim().isEmpty()) {
            errors.add("Country is required");
        } else if (!COUNTRY_CODE_PATTERN.matcher(location.getCountry()).matches()) {
            errors.add("Country must be a valid 2-letter code");
        }

        // Validate timezone (optional but if provided)
        if (location.getTimezone() != null && !location.getTimezone().trim().isEmpty()) {
            if (!TIMEZONE_PATTERN.matcher(location.getTimezone()).matches()) {
                errors.add("Invalid timezone format");
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate weather alert data
     */
    public ValidationResult validateWeatherAlert(String alertType, String title, String description,
            String severity, LocalDateTime alertTime, LocalDateTime expiresAt) {
        List<String> errors = new ArrayList<>();

        // Validate alert type
        if (alertType == null || alertType.trim().isEmpty()) {
            errors.add("Alert type is required");
        } else if (!ALERT_TYPE_PATTERN.matcher(alertType).matches()) {
            errors.add("Alert type must contain only uppercase letters and underscores");
        }

        // Validate title
        if (title == null || title.trim().isEmpty()) {
            errors.add("Alert title is required");
        } else if (title.length() > 200) {
            errors.add("Alert title must be less than 200 characters");
        }

        // Validate description
        if (description == null || description.trim().isEmpty()) {
            errors.add("Alert description is required");
        } else if (description.length() > 1000) {
            errors.add("Alert description must be less than 1000 characters");
        }

        // Validate severity
        if (severity == null || severity.trim().isEmpty()) {
            errors.add("Alert severity is required");
        } else if (!SEVERITY_PATTERN.matcher(severity).matches()) {
            errors.add("Alert severity must be LOW, MEDIUM, HIGH, or CRITICAL");
        }

        // Validate dates
        if (alertTime == null) {
            errors.add("Alert time is required");
        } else if (alertTime.isAfter(LocalDateTime.now().plusDays(30))) {
            errors.add("Alert time cannot be more than 30 days in the future");
        }

        if (expiresAt == null) {
            errors.add("Alert expiration time is required");
        } else if (alertTime != null && expiresAt.isBefore(alertTime)) {
            errors.add("Alert expiration time must be after alert time");
        } else if (expiresAt.isAfter(LocalDateTime.now().plusDays(365))) {
            errors.add("Alert expiration time cannot be more than 365 days in the future");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate weather data ranges
     */
    public ValidationResult validateWeatherData(BigDecimal temperature, BigDecimal humidity,
            BigDecimal pressure, BigDecimal windSpeed) {
        List<String> errors = new ArrayList<>();

        // Validate temperature
        if (temperature != null) {
            if (temperature.compareTo(MIN_TEMPERATURE) < 0 || temperature.compareTo(MAX_TEMPERATURE) > 0) {
                errors.add("Temperature must be between -100°C and 70°C");
            }
        }

        // Validate humidity
        if (humidity != null) {
            if (humidity.compareTo(MIN_HUMIDITY) < 0 || humidity.compareTo(MAX_HUMIDITY) > 0) {
                errors.add("Humidity must be between 0% and 100%");
            }
        }

        // Validate pressure
        if (pressure != null) {
            if (pressure.compareTo(MIN_PRESSURE) < 0 || pressure.compareTo(MAX_PRESSURE) > 0) {
                errors.add("Pressure must be between 800 hPa and 1200 hPa");
            }
        }

        // Validate wind speed
        if (windSpeed != null) {
            if (windSpeed.compareTo(BigDecimal.ZERO) < 0 || windSpeed.compareTo(new BigDecimal("500")) > 0) {
                errors.add("Wind speed must be between 0 and 500 km/h");
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate pagination parameters
     */
    public ValidationResult validatePagination(int page, int size) {
        List<String> errors = new ArrayList<>();

        if (page < 0) {
            errors.add("Page number must be non-negative");
        }

        if (size <= 0) {
            errors.add("Page size must be positive");
        } else if (size > 100) {
            errors.add("Page size cannot exceed 100");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate date range
     */
    public ValidationResult validateDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        List<String> errors = new ArrayList<>();

        if (startDate == null) {
            errors.add("Start date is required");
        }

        if (endDate == null) {
            errors.add("End date is required");
        }

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                errors.add("Start date must be before end date");
            }

            if (startDate.isBefore(LocalDateTime.now().minusYears(10))) {
                errors.add("Start date cannot be more than 10 years in the past");
            }

            if (endDate.isAfter(LocalDateTime.now().plusDays(30))) {
                errors.add("End date cannot be more than 30 days in the future");
            }

            if (java.time.Duration.between(startDate, endDate).toDays() > 365) {
                errors.add("Date range cannot exceed 365 days");
            }
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate search query parameters
     */
    public ValidationResult validateSearchQuery(String query) {
        List<String> errors = new ArrayList<>();

        if (query == null || query.trim().isEmpty()) {
            errors.add("Search query cannot be empty");
        } else if (query.length() < 2) {
            errors.add("Search query must be at least 2 characters long");
        } else if (query.length() > 100) {
            errors.add("Search query cannot exceed 100 characters");
        } else if (!LOCATION_NAME_PATTERN.matcher(query).matches()) {
            errors.add("Search query contains invalid characters");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate user ID
     */
    public ValidationResult validateUserId(Long userId) {
        List<String> errors = new ArrayList<>();

        if (userId == null) {
            errors.add("User ID is required");
        } else if (userId <= 0) {
            errors.add("User ID must be positive");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Validate location ID
     */
    public ValidationResult validateLocationId(Long locationId) {
        List<String> errors = new ArrayList<>();

        if (locationId == null) {
            errors.add("Location ID is required");
        } else if (locationId <= 0) {
            errors.add("Location ID must be positive");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }

    /**
     * Sanitize input string
     */
    public String sanitizeInput(String input) {
        if (input == null) {
            return null;
        }

        return input.trim()
                .replaceAll("[<>\"'&]", "") // Remove potentially dangerous characters
                .replaceAll("\\s+", " "); // Normalize whitespace
    }

    /**
     * Check if coordinates are within valid range
     */
    public boolean areValidCoordinates(BigDecimal latitude, BigDecimal longitude) {
        return isValidLatitude(latitude) && isValidLongitude(longitude);
    }

    // Helper methods
    private boolean isValidLatitude(BigDecimal latitude) {
        return latitude != null &&
                latitude.compareTo(MIN_LATITUDE) >= 0 &&
                latitude.compareTo(MAX_LATITUDE) <= 0;
    }

    private boolean isValidLongitude(BigDecimal longitude) {
        return longitude != null &&
                longitude.compareTo(MIN_LONGITUDE) >= 0 &&
                longitude.compareTo(MAX_LONGITUDE) <= 0;
    }

    /**
     * Check if string is safe for logging
     */
    public boolean isSafeForLogging(String input) {
        if (input == null) {
            return true;
        }

        // Check for patterns that might indicate log injection
        return !input.contains("\n") &&
                !input.contains("\r") &&
                !input.contains("\t") &&
                input.length() <= 500;
    }

    /**
     * Validate forecast days parameter
     */
    public ValidationResult validateForecastDays(int days) {
        List<String> errors = new ArrayList<>();

        if (days <= 0) {
            errors.add("Forecast days must be positive");
        } else if (days > 16) {
            errors.add("Forecast days cannot exceed 16");
        }

        return new ValidationResult(errors.isEmpty(), errors);
    }
}
