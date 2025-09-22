package com.weather_found.weather_app.modules.location.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import java.time.LocalDate;

/**
 * DTO for updating an existing location
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLocationRequest {

    @Size(max = 255, message = "Location name must not exceed 255 characters")
    private String name;

    @DecimalMin(value = "-90.0", message = "Latitude must be between -90 and 90")
    @DecimalMax(value = "90.0", message = "Latitude must be between -90 and 90")
    @Digits(integer = 2, fraction = 8, message = "Invalid latitude format")
    private BigDecimal latitude;

    @DecimalMin(value = "-180.0", message = "Longitude must be between -180 and 180")
    @DecimalMax(value = "180.0", message = "Longitude must be between -180 and 180")
    @Digits(integer = 3, fraction = 8, message = "Invalid longitude format")
    private BigDecimal longitude;

    @Size(max = 100, message = "Country name must not exceed 100 characters")
    private String country;

    @Size(max = 100, message = "State name must not exceed 100 characters")
    private String state;

    @Size(max = 100, message = "City name must not exceed 100 characters")
    private String city;

    @Size(max = 1000, message = "Address must not exceed 1000 characters")
    private String address;

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    private String timezone;

    @Min(value = -500, message = "Elevation must be greater than -500 meters")
    @Max(value = 10000, message = "Elevation must be less than 10000 meters")
    private Integer elevation;

    private LocalDate beginDate;
    private LocalDate endDate;
}