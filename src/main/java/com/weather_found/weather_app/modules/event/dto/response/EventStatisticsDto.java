package com.weather_found.weather_app.modules.event.dto.response;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventStatisticsDto {

    private Long totalEvents;

    private Long upcomingEvents;

    private Long activeEvents;

    private Long completedEvents;

    private Long cancelledEvents;

    private Long outdoorEvents;

    private Long indoorEvents;

    private Map<String, Long> eventsByType;

    private Map<String, Long> eventsByStatus;
}
