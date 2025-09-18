package com.weather_found.weather_app.modules.analytics.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

/**
 * DTO for API usage analytics response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiUsageResponseDto {

    @JsonProperty("total_requests")
    private Long totalRequests;

    @JsonProperty("unique_users")
    private Long uniqueUsers;

    @JsonProperty("average_response_time")
    private Double averageResponseTime;

    @JsonProperty("error_rate")
    private Double errorRate;

    @JsonProperty("peak_hour")
    private Integer peakHour;

    @JsonProperty("most_popular_endpoints")
    private List<EndpointUsage> mostPopularEndpoints;

    @JsonProperty("usage_by_hour")
    private List<HourlyUsage> usageByHour;

    @JsonProperty("period_start")
    private Instant periodStart;

    @JsonProperty("period_end")
    private Instant periodEnd;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EndpointUsage {
        @JsonProperty("endpoint")
        private String endpoint;

        @JsonProperty("method")
        private String method;

        @JsonProperty("request_count")
        private Long requestCount;

        @JsonProperty("average_response_time")
        private Double averageResponseTime;

        @JsonProperty("error_rate")
        private Double errorRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyUsage {
        @JsonProperty("hour")
        private Integer hour;

        @JsonProperty("request_count")
        private Long requestCount;

        @JsonProperty("unique_users")
        private Long uniqueUsers;

        @JsonProperty("average_response_time")
        private Double averageResponseTime;
    }
}