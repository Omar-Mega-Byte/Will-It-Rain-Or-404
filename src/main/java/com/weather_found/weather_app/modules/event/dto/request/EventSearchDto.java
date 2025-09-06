package com.weather_found.weather_app.modules.event.dto.request;

import java.time.LocalDateTime;

import com.weather_found.weather_app.modules.event.model.enums.EventStatus;
import com.weather_found.weather_app.modules.event.model.enums.EventType;

import lombok.Data;

@Data
public class EventSearchDto {

    private String eventName;

    private EventType eventType;

    private EventStatus eventStatus;

    private Boolean isOutdoor;

    private LocalDateTime startDateFrom;

    private LocalDateTime startDateTo;

    private LocalDateTime endDateFrom;

    private LocalDateTime endDateTo;

    // Pagination
    private Integer page = 0;

    private Integer size = 10;

    private String sortBy = "startDate";

    private String sortDirection = "ASC";
}
