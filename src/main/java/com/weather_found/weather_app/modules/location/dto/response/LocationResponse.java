package com.weather_found.weather_app.modules.location.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalDate;

/**
 * DTO for location response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationResponse {

    private Long id;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String country;
    private String state;
    private String city;
    private String address;
    private String timezone;
    private Integer elevation;
    private LocalDate beginDate;
    private LocalDate endDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}