package com.weather_found.weather_app.modules.location.dto.ml;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for passing essential location and time window features to ML pipeline
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationMLRequest {

    private BigDecimal latitude; // Required: spatial input
    private BigDecimal longitude; // Required: spatial input
    private String timezone; // Helpful for aligning daily cycles
    private Integer elevation; // Context for climate features

    private LocalDate beginDate; // Start of DOY window
    private LocalDate endDate; // End of DOY window
}
