package com.weather_found.weather_app.modules.weather.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Weather data model for Redis caching
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeatherData implements Serializable {

    @JsonProperty("location")
    private LocationInfo location;

    @JsonProperty("current")
    private CurrentWeather current;

    @JsonProperty("forecast")
    private WeatherForecast forecast;

    @JsonProperty("lastUpdated")
    private LocalDateTime lastUpdated;

    @JsonProperty("dataSource")
    private String dataSource;

    @JsonProperty("accuracy")
    private String accuracy;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationInfo implements Serializable {
        private String name;
        private String country;
        private Coordinates coordinates;
        private String timezone;
        private String localTime;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Coordinates implements Serializable {
            private double lat;
            private double lng;
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CurrentWeather implements Serializable {
        private String temperature;
        private String feelsLike;
        private String condition;
        private String humidity;
        private String pressure;
        private String visibility;
        private String uvIndex;
        private String windSpeed;
        private String windDirection;
        private String windGust;
        private String cloudCover;
        private String dewPoint;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WeatherForecast implements Serializable {
        private String period;
        private LocalDateTime generatedAt;
        private java.util.List<DailyForecast> daily;
        private ForecastSummary summary;

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DailyForecast implements Serializable {
            private String date;
            private String dayOfWeek;
            private Temperature temperature;
            private String condition;
            private String humidity;
            private Precipitation precipitation;
            private Wind wind;
            private String uvIndex;
            private String sunrise;
            private String sunset;

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Temperature implements Serializable {
                private String min;
                private String max;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Precipitation implements Serializable {
                private String chance;
                private String amount;
            }

            @Data
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Wind implements Serializable {
                private String speed;
                private String direction;
            }
        }

        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public static class ForecastSummary implements Serializable {
            private String averageTemp;
            private String totalPrecipitation;
            private int rainDays;
            private int sunnyDays;
            private String dominantCondition;
        }
    }
}
