package com.weather_found.weather_app.modules.event.dto.response;

import java.time.LocalDateTime;

import com.weather_found.weather_app.modules.event.model.enums.EventStatus;
import com.weather_found.weather_app.modules.event.model.enums.EventType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventSummaryDto {

    private Long id;

    private String eventName;

    private EventType eventType;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean isOutdoor;

    private EventStatus eventStatus;

    private Integer participantCount;

    private LocalDateTime createdAt;

    // Location will be handled when location module is integrated
    // private String locationName;
}
