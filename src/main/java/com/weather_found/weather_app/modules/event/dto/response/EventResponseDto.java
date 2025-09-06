package com.weather_found.weather_app.modules.event.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import com.weather_found.weather_app.modules.event.model.enums.EventStatus;
import com.weather_found.weather_app.modules.event.model.enums.EventType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventResponseDto {

    private Long id;

    private String eventName;

    private String eventDescription;

    private EventType eventType;

    private LocalDateTime startDate;

    private LocalDateTime endDate;

    private Boolean isOutdoor;

    private EventStatus eventStatus;

    private List<EventUserDto> users;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Location will be handled when location module is integrated
    // private LocationResponseDto location;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventUserDto {
        private Long id;
        private String username;
        private String firstName;
        private String lastName;
        private String email;
    }
}
