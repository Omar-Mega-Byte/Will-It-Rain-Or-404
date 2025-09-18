package com.weather_found.weather_app.modules.location.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for location search requests
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationSearchRequest {

    @NotBlank(message = "Search query is required")
    @Size(min = 1, max = 100, message = "Search query must be between 1 and 100 characters")
    private String query;

    private String country;
    private String state;
    private String city;
}