package com.weather_found.weather_app.modules.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

import java.time.LocalDate;

/**
 * DTO for simplified location response used in lists
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationSummaryResponse {

    private Long id;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String country;
    private String state;
    private String city;
    private String timezone;
    private LocalDate beginDate;
    private LocalDate endDate;
}