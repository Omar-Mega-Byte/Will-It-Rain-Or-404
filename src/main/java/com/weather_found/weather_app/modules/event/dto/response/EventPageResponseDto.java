package com.weather_found.weather_app.modules.event.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventPageResponseDto {

    private List<EventSummaryDto> events;

    private Integer currentPage;

    private Integer totalPages;

    private Long totalElements;

    private Integer size;

    private Boolean hasNext;

    private Boolean hasPrevious;

    private Boolean isFirst;

    private Boolean isLast;
}
