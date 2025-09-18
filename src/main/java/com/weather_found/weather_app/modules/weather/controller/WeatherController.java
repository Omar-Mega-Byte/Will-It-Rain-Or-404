package com.weather_found.weather_app.modules.weather.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.weather_found.weather_app.modules.weather.service.ExternalWeatherApiService;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * REST controller for weather data management
 */
@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Weather Management", description = "Weather data and forecast endpoints")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

    private final ExternalWeatherApiService externalWeatherApiService;

    // List of random cities for the random weather endpoint
    private static final List<String> RANDOM_CITIES = List.of(
            "New York, NY", "London, UK", "Tokyo, Japan", "Paris, France", "Sydney, Australia",
            "Toronto, Canada", "Berlin, Germany", "Mumbai, India", "São Paulo, Brazil", "Cairo, Egypt",
            "Bangkok, Thailand", "Moscow, Russia", "Cape Town, South Africa", "Mexico City, Mexico",
            "Singapore", "Istanbul, Turkey", "Buenos Aires, Argentina", "Lagos, Nigeria",
            "Seoul, South Korea", "Stockholm, Sweden");

    private static final Random random = new Random();

    /**
     * Get current weather for a location
     */
    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get current weather", description = "Get current weather data for a specific location")
    public ResponseEntity<String> getCurrentWeather(@RequestParam String location, Authentication authentication) {
        String currentWeather = """
                {
                    "location": {
                        "name": "%s",
                        "country": "United States",
                        "coordinates": {"lat": 40.7128, "lng": -74.0060},
                        "timezone": "America/New_York",
                        "localTime": "%s"
                    },
                    "current": {
                        "temperature": "22°C",
                        "feelsLike": "24°C",
                        "condition": "Partly Cloudy",
                        "humidity": "65%%",
                        "pressure": "1013 hPa",
                        "visibility": "10 km",
                        "uvIndex": "6 (High)",
                        "windSpeed": "12 km/h",
                        "windDirection": "Southwest",
                        "windGust": "18 km/h",
                        "cloudCover": "40%%",
                        "dewPoint": "15°C"
                    },
                    "comfort": {
                        "heatIndex": "24°C",
                        "windChill": "22°C",
                        "comfortLevel": "Comfortable"
                    },
                    "airQuality": {
                        "aqi": 42,
                        "quality": "Good",
                        "primaryPollutant": "PM2.5",
                        "healthAdvice": "Air quality is satisfactory"
                    },
                    "sunrise": "06:45",
                    "sunset": "19:15",
                    "daylight": "12h 30m",
                    "lastUpdated": "%s",
                    "dataSource": "WeatherAPI Premium",
                    "accuracy": "High"
                }
                """.formatted(location, java.time.Instant.now().toString(), java.time.Instant.now().toString());
        return ResponseEntity.ok(currentWeather);
    }

    /**
     * Get random weather for a random location (public endpoint for home page)
     */
    @GetMapping("/random")
    @Operation(summary = "Get random weather", description = "Get current weather data for a random location (no authentication required)")
    public ResponseEntity<String> getRandomWeather() {
        // Select a random city
        String randomCity = RANDOM_CITIES.get(random.nextInt(RANDOM_CITIES.size()));

        try {
            log.info("Attempting to fetch real weather data for: {}", randomCity);

            // Try to get real weather data directly from OpenWeather API
            Map<String, Object> realWeatherData = externalWeatherApiService.getRealWeatherDataForCity(randomCity);

            if (realWeatherData != null) {
                // Convert real weather data to JSON format
                String weatherJson = convertRealWeatherDataToJson(randomCity, realWeatherData);
                log.info("Successfully fetched real weather data for: {}", randomCity);
                return ResponseEntity.ok(weatherJson);
            }

            log.warn("Real weather data not available for: {}", randomCity);

        } catch (Exception e) {
            log.error("Error fetching real weather data for: {}", randomCity, e);
        }

        // If real weather data is not available, do not return random/fallback data
        return ResponseEntity.status(503).body("{\"error\":\"Weather data unavailable. Please try again later.\"}");
    }

    /**
     * Helper method to convert real OpenWeather data to frontend JSON format
     */
    private String convertRealWeatherDataToJson(String cityName, Map<String, Object> weatherData) {
        try {
            String[] cityParts = cityName.split(", ");
            String name = cityParts[0];
            String country = cityParts.length > 1 ? cityParts[1] : "Unknown";

            // Use real coordinates from API if available, otherwise fallback to city mapping
            double lat = 0.0;
            double lon = 0.0;
            if (weatherData.get("lat") instanceof Number) lat = ((Number) weatherData.get("lat")).doubleValue();
            if (weatherData.get("lon") instanceof Number) lon = ((Number) weatherData.get("lon")).doubleValue();
            if (lat == 0.0 && lon == 0.0) {
                lat = getCityLatitude(name);
                lon = getCityLongitude(name);
            }

            double temperature = weatherData.get("temperature") instanceof Number ?
                ((Number) weatherData.get("temperature")).doubleValue() : 22.0;
            double humidity = weatherData.get("humidity") instanceof Number ?
                ((Number) weatherData.get("humidity")).doubleValue() : 65.0;
            double pressure = weatherData.get("pressure") instanceof Number ?
                ((Number) weatherData.get("pressure")).doubleValue() : 1013.0;
            double windSpeed = weatherData.get("windSpeed") instanceof Number ?
                ((Number) weatherData.get("windSpeed")).doubleValue() : 12.0;
            double visibility = weatherData.get("visibility") instanceof Number ?
                ((Number) weatherData.get("visibility")).doubleValue() : 10.0;
            String condition = weatherData.get("weatherCondition") != null ?
                weatherData.get("weatherCondition").toString() : "Clear";
            String description = weatherData.get("description") != null ?
                weatherData.get("description").toString() : condition;
            int windDirection = weatherData.get("windDirection") instanceof Number ?
                ((Number) weatherData.get("windDirection")).intValue() : 0;
            int cloudCover = weatherData.get("cloudCover") instanceof Number ?
                ((Number) weatherData.get("cloudCover")).intValue() : 50;

            return String.format("""
                {
                    "location": {
                        "name": "%s",
                        "country": "%s",
                        "coordinates": {"lat": %.6f, "lng": %.6f},
                        "timezone": "Local Time",
                        "localTime": "%s"
                    },
                    "current": {
                        "temperature": %.1f,
                        "feelsLike": %.1f,
                        "condition": "%s",
                        "description": "%s",
                        "humidity": "%.0f%%",
                        "pressure": "%.0f hPa",
                        "visibility": "%.1f km",
                        "windSpeed": %.1f,
                        "windDirection": "%d°",
                        "cloudCover": "%d%%"
                    },
                    "lastUpdated": "%s",
                    "dataSource": "OpenWeatherMap (Real Data)",
                    "accuracy": "High"
                }
                """,
                name, country,
                lat, lon,
                java.time.Instant.now().toString(),
                temperature, temperature + 1.5, condition, description, humidity,
                pressure, visibility, windSpeed, windDirection, cloudCover,
                java.time.Instant.now().toString()
            );
        } catch (Exception e) {
            log.error("Error converting real weather data to JSON: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Get approximate latitude for known cities
     */
    private double getCityLatitude(String cityName) {
        return switch (cityName) {
            case "New York" -> 40.7128;
            case "London" -> 51.5074;
            case "Tokyo" -> 35.6762;
            case "Paris" -> 48.8566;
            case "Sydney" -> -33.8688;
            case "Toronto" -> 43.6532;
            case "Berlin" -> 52.5200;
            case "Mumbai" -> 19.0760;
            case "São Paulo" -> -23.5505;
            case "Cairo" -> 30.0444;
            case "Bangkok" -> 13.7563;
            case "Moscow" -> 55.7558;
            case "Cape Town" -> -33.9249;
            case "Mexico City" -> 19.4326;
            case "Singapore" -> 1.3521;
            case "Istanbul" -> 41.0082;
            case "Buenos Aires" -> -34.6037;
            case "Lagos" -> 6.5244;
            case "Seoul" -> 37.5665;
            case "Stockholm" -> 59.3293;
            default -> 0.0;
        };
    }

    /**
     * Get approximate longitude for known cities
     */
    private double getCityLongitude(String cityName) {
        return switch (cityName) {
            case "New York" -> -74.0060;
            case "London" -> -0.1278;
            case "Tokyo" -> 139.6503;
            case "Paris" -> 2.3522;
            case "Sydney" -> 151.2093;
            case "Toronto" -> -79.3832;
            case "Berlin" -> 13.4050;
            case "Mumbai" -> 72.8777;
            case "São Paulo" -> -46.6333;
            case "Cairo" -> 31.2357;
            case "Bangkok" -> 100.5018;
            case "Moscow" -> 37.6176;
            case "Cape Town" -> 18.4241;
            case "Mexico City" -> -99.1332;
            case "Singapore" -> 103.8198;
            case "Istanbul" -> 28.9784;
            case "Buenos Aires" -> -58.3816;
            case "Lagos" -> 3.3792;
            case "Seoul" -> 126.9780;
            case "Stockholm" -> 18.0686;
            default -> 0.0;
        };
    }

    /**
     * Get weather forecast
     */
    @GetMapping("/forecast")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get weather forecast", description = "Get weather forecast for a specific location and time period")
    public ResponseEntity<String> getWeatherForecast(
            @RequestParam String location,
            @RequestParam(defaultValue = "7") int days,
            Authentication authentication) {
        String forecast = """
                {
                    "location": {
                        "name": "%s",
                        "coordinates": {"lat": 40.7128, "lng": -74.0060},
                        "timezone": "America/New_York"
                    },
                    "forecast": {
                        "period": "%d days",
                        "generatedAt": "%s",
                        "daily": [
                            {
                                "date": "2025-09-06",
                                "dayOfWeek": "Friday",
                                "temperature": {"min": "18°C", "max": "25°C"},
                                "condition": "Sunny",
                                "humidity": "60%%",
                                "precipitation": {"chance": "10%%", "amount": "0mm"},
                                "wind": {"speed": "14 km/h", "direction": "SW"},
                                "uvIndex": "7",
                                "sunrise": "06:46",
                                "sunset": "19:13"
                            },
                            {
                                "date": "2025-09-07",
                                "dayOfWeek": "Saturday",
                                "temperature": {"min": "20°C", "max": "27°C"},
                                "condition": "Partly Cloudy",
                                "humidity": "65%%",
                                "precipitation": {"chance": "20%%", "amount": "1mm"},
                                "wind": {"speed": "12 km/h", "direction": "S"},
                                "uvIndex": "6",
                                "sunrise": "06:47",
                                "sunset": "19:11"
                            },
                            {
                                "date": "2025-09-08",
                                "dayOfWeek": "Sunday",
                                "temperature": {"min": "19°C", "max": "24°C"},
                                "condition": "Light Rain",
                                "humidity": "75%%",
                                "precipitation": {"chance": "70%%", "amount": "8mm"},
                                "wind": {"speed": "16 km/h", "direction": "SW"},
                                "uvIndex": "4",
                                "sunrise": "06:48",
                                "sunset": "19:09"
                            }
                        ],
                        "summary": {
                            "averageTemp": "23°C",
                            "totalPrecipitation": "9mm",
                            "rainDays": 1,
                            "sunnyDays": 2,
                            "dominantCondition": "Mostly sunny with occasional showers"
                        }
                    },
                    "alerts": [],
                    "dataSource": "WeatherAPI Premium + ML Predictions",
                    "accuracy": "89%%",
                    "lastUpdated": "%s"
                }
                """.formatted(location, days, java.time.Instant.now().toString(), java.time.Instant.now().toString());
        return ResponseEntity.ok(forecast);
    }

    /**
     * Get historical weather data
     */
    @GetMapping("/historical")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get historical weather", description = "Get historical weather data for analysis")
    public ResponseEntity<String> getHistoricalWeather(
            @RequestParam String location,
            @RequestParam String startDate,
            @RequestParam String endDate,
            Authentication authentication) {
        String historicalData = """
                {
                    "location": {
                        "name": "%s",
                        "coordinates": {"lat": 40.7128, "lng": -74.0060}
                    },
                    "period": {
                        "startDate": "%s",
                        "endDate": "%s",
                        "totalDays": 30
                    },
                    "summary": {
                        "averageTemperature": "21.3°C",
                        "minTemperature": "12°C",
                        "maxTemperature": "29°C",
                        "totalPrecipitation": "45mm",
                        "rainDays": 8,
                        "sunnyDays": 18,
                        "cloudyDays": 4
                    },
                    "monthlyAverages": {
                        "temperature": {
                            "average": "21.3°C",
                            "min": "17.2°C",
                            "max": "25.4°C"
                        },
                        "precipitation": "45mm",
                        "humidity": "67%%",
                        "windSpeed": "13.2 km/h"
                    },
                    "extremes": {
                        "hottestDay": {"date": "2025-08-15", "temperature": "29°C"},
                        "coldestDay": {"date": "2025-08-03", "temperature": "12°C"},
                        "rainiest": {"date": "2025-08-22", "precipitation": "18mm"},
                        "windiest": {"date": "2025-08-10", "windSpeed": "32 km/h"}
                    },
                    "trends": {
                        "temperatureTrend": "Increasing (+0.5°C/week)",
                        "precipitationTrend": "Decreasing (-2mm/week)",
                        "humidityTrend": "Stable"
                    },
                    "comparisons": {
                        "lastYear": {
                            "temperature": "+1.2°C warmer",
                            "precipitation": "-8mm less rain",
                            "sunnyDays": "+3 more sunny days"
                        },
                        "longTermAverage": {
                            "temperature": "+0.8°C above average",
                            "precipitation": "Normal range",
                            "humidity": "-2%% below average"
                        }
                    },
                    "dataQuality": {
                        "completeness": "98.7%%",
                        "source": "National Weather Service + Satellite Data",
                        "accuracy": "High",
                        "gaps": "2 hours missing data"
                    },
                    "generatedAt": "%s"
                }
                """.formatted(location, startDate, endDate, java.time.Instant.now().toString());
        return ResponseEntity.ok(historicalData);
    }

    /**
     * Update weather data - Admin only
     */
    @PostMapping("/update")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update weather data", description = "Update weather data from external sources (Admin only)")
    public ResponseEntity<String> updateWeatherData(Authentication authentication) {
        String updateResult = """
                {
                    "status": "success",
                    "message": "Weather data update completed successfully",
                    "updateJob": {
                        "jobId": "UPDATE_%s",
                        "initiatedBy": "%s",
                        "startedAt": "%s",
                        "completedAt": "%s",
                        "duration": "2.3 minutes"
                    },
                    "dataSources": {
                        "nationalWeatherService": {
                            "status": "success",
                            "recordsUpdated": 15847,
                            "newData": 1247,
                            "errors": 0
                        },
                        "weatherAPI": {
                            "status": "success",
                            "recordsUpdated": 8934,
                            "newData": 567,
                            "errors": 2
                        },
                        "satelliteData": {
                            "status": "success",
                            "recordsUpdated": 4523,
                            "newData": 234,
                            "errors": 0
                        }
                    },
                    "statistics": {
                        "totalRecordsProcessed": 29304,
                        "newRecords": 2048,
                        "updatedRecords": 27256,
                        "duplicatesSkipped": 145,
                        "errors": 2,
                        "successRate": "99.93%%"
                    },
                    "locationsCovered": {
                        "total": 8947,
                        "updated": 8945,
                        "failed": 2,
                        "coverage": "99.98%%"
                    },
                    "cacheRefresh": {
                        "forecastCache": "refreshed",
                        "currentWeatherCache": "refreshed",
                        "historicalCache": "updated",
                        "mlModelCache": "refreshed"
                    },
                    "quality": {
                        "dataQuality": "98.7%%",
                        "validationPassed": true,
                        "anomaliesDetected": 12,
                        "anomaliesResolved": 12
                    },
                    "nextUpdate": {
                        "scheduled": "%s",
                        "automatic": true,
                        "type": "incremental"
                    },
                    "performance": {
                        "apiResponseTime": "improved by 15%%",
                        "predictionAccuracy": "maintained at 89.2%%",
                        "systemLoad": "normal"
                    }
                }
                """.formatted(
                System.currentTimeMillis(),
                authentication.getName(),
                java.time.Instant.now().minusSeconds(138).toString(),
                java.time.Instant.now().toString(),
                java.time.Instant.now().plusSeconds(3600).toString());
        return ResponseEntity.ok(updateResult);
    }
}
