package com.weather_found.weather_app.modules.event.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.weather_found.weather_app.modules.event.model.enums.EventStatus;
import com.weather_found.weather_app.modules.event.model.enums.EventType;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EventUpdateDto {

    @Size(max = 255, message = "Event name must not exceed 255 characters")
    private String eventName;

    @Size(max = 1000, message = "Event description must not exceed 1000 characters")
    private String eventDescription;

    private EventType eventType;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean isOutdoor;

    private EventStatus eventStatus;

    private List<Long> userIds;

    // Location will be handled when location module is integrated
    // private Long locationId;
}
