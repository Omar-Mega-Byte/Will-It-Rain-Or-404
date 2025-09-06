package com.weather_found.weather_app.modules.event.dto.request;

import java.time.LocalDateTime;
import java.util.List;

import com.weather_found.weather_app.modules.event.model.enums.EventStatus;
import com.weather_found.weather_app.modules.event.model.enums.EventType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class EventCreateDto {

    @NotBlank(message = "Event name is required")
    @Size(max = 255, message = "Event name must not exceed 255 characters")
    private String eventName;

    @NotBlank(message = "Event description is required")
    @Size(max = 1000, message = "Event description must not exceed 1000 characters")
    private String eventDescription;

    @NotNull(message = "Event type is required")
    private EventType eventType;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @NotNull(message = "Outdoor flag is required")
    private Boolean isOutdoor;

    private EventStatus eventStatus = EventStatus.SCHEDULED;

    private List<Long> userIds;

    // Location will be handled when location module is integrated
    // private Long locationId;
}
