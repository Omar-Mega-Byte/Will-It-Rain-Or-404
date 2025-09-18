package com.weather_found.weather_app.modules.weather.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.weather_found.weather_app.modules.weather.model.Location;
import com.weather_found.weather_app.modules.weather.model.WeatherDataEntity;
import com.weather_found.weather_app.modules.weather.repository.WeatherLocationRepository;
import com.weather_found.weather_app.modules.weather.repository.WeatherDataRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Service for integrating with external weather APIs
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ExternalWeatherApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final WeatherLocationRepository locationRepository;
    private final WeatherDataRepository weatherDataRepository;

    @Value("${weather.api.openweathermap.key}")
    private String openWeatherMapApiKey;

    @Value("${weather.api.nasa.key:demo-key}")
    private String nasaApiKey;

    @Value("${weather.api.timeout:5000}")
    private int apiTimeout;

    // API URLs
    private static final String OPENWEATHERMAP_CURRENT_URL = "https://api.openweathermap.org/data/2.5/weather";
    private static final String OPENWEATHERMAP_FORECAST_URL = "https://api.openweathermap.org/data/2.5/forecast";
    private static final String NASA_POWER_URL = "https://power.larc.nasa.gov/api/temporal/daily/point";

    /**
     * Fetch current weather from OpenWeatherMap API (no Redis, always live)
     */
    public CompletableFuture<Map<String, Object>> fetchCurrentWeatherFromOpenWeatherMap(String locationName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = String.format("%s?q=%s&appid=%s&units=metric",
                        OPENWEATHERMAP_CURRENT_URL, locationName, openWeatherMapApiKey);
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> weatherData = objectMapper.readValue(response.getBody(), Map.class);
                    return weatherData;
                } else {
                    log.warn("OpenWeatherMap API returned non-2xx for {}: {}", locationName, response.getStatusCode());
                    return null;
                }
            } catch (Exception e) {
                log.error("Error fetching weather from OpenWeatherMap for {}", locationName, e);
                return null;
            }
        });
    }

    /**
     * Fetch weather forecast from OpenWeatherMap API (no Redis, always live)
     */
    public CompletableFuture<Map<String, Object>> fetchForecastFromOpenWeatherMap(String locationName, int days) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = String.format("%s?q=%s&appid=%s&units=metric&cnt=%d",
                        OPENWEATHERMAP_FORECAST_URL, locationName, openWeatherMapApiKey, days * 8);
                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> forecastData = parseOpenWeatherMapForecastResponse(response.getBody());
                    return forecastData;
                } else {
                    log.warn("Failed to fetch forecast from OpenWeatherMap: {}", response.getStatusCode());
                    return null;
                }
            } catch (Exception e) {
                log.error("Error fetching forecast from OpenWeatherMap for location: {}", locationName, e);
                return null;
            }
        });
    }

    /**
     * Fetch historical weather data from NASA POWER API
     */
    public CompletableFuture<Map<String, Object>> fetchHistoricalDataFromNASA(
            BigDecimal latitude, BigDecimal longitude, String startDate, String endDate) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String url = String.format(
                        "%s?start=%s&end=%s&latitude=%s&longitude=%s&community=ag&parameters=T2M,PRECTOT,WS2M,RH2M&format=json",
                        NASA_POWER_URL, startDate, endDate, latitude, longitude);

                ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> historicalData = parseNASAResponse(response.getBody());
                    return historicalData;
                } else {
                    log.warn("Failed to fetch from NASA POWER: {}", response.getStatusCode());
                    return generateMockHistoricalData(latitude, longitude, startDate, endDate, "NASA POWER");
                }

            } catch (Exception e) {
                log.error("Error fetching historical data from NASA", e);
                return generateMockHistoricalData(latitude, longitude, startDate, endDate, "NASA POWER");
            }
        });
    }

    /**
     * Store weather data in database
     */
    @Async
    public void storeWeatherData(String locationName, Map<String, Object> weatherData) {
        try {
            Optional<Location> locationOpt = locationRepository.findByNameIgnoreCase(locationName);
            Location location;

            if (locationOpt.isPresent()) {
                location = locationOpt.get();
            } else {
                // Create new location if not exists
                location = createLocationFromWeatherData(locationName, weatherData);
                location = locationRepository.save(location);
            }

            WeatherDataEntity weatherEntity = convertToWeatherEntity(location, weatherData);
            weatherDataRepository.save(weatherEntity);

            log.debug("Stored weather data for location: {}", locationName);

        } catch (Exception e) {
            log.error("Error storing weather data for location: {}", locationName, e);
        }
    }

    /**
     * Aggregate weather data from multiple sources
     */
    public Map<String, Object> aggregateWeatherData(String locationName) {
        try {
            List<CompletableFuture<Map<String, Object>>> futures = Arrays.asList(
                    fetchCurrentWeatherFromOpenWeatherMap(locationName)
            // Add more API calls here when available
            );

            CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                    futures.toArray(new CompletableFuture[0]));

            CompletableFuture<List<Map<String, Object>>> allResults = allFutures.thenApply(v -> futures.stream()
                    .map(CompletableFuture::join)
                    .filter(Objects::nonNull)
                    .toList());

            List<Map<String, Object>> results = allResults.get();

            if (results.isEmpty()) {
                return generateMockCurrentWeather(locationName, "Aggregated");
            }

            return aggregateResults(results, locationName);

        } catch (Exception e) {
            log.error("Error aggregating weather data for location: {}", locationName, e);
            return generateMockCurrentWeather(locationName, "Aggregated");
        }
    }

    // Helper methods
    private Map<String, Object> parseOpenWeatherMapResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            Map<String, Object> weatherData = new HashMap<>();

            // Extract coordinates
            JsonNode coord = root.path("coord");
            if (coord.has("lat")) weatherData.put("lat", coord.get("lat").asDouble());
            if (coord.has("lon")) weatherData.put("lon", coord.get("lon").asDouble());

            weatherData.put("temperature", root.path("main").path("temp").asDouble());
            weatherData.put("humidity", root.path("main").path("humidity").asDouble());
            weatherData.put("pressure", root.path("main").path("pressure").asDouble());
            weatherData.put("windSpeed", root.path("wind").path("speed").asDouble());
            weatherData.put("windDirection", root.path("wind").path("deg").asInt());
            weatherData.put("cloudCover", root.path("clouds").path("all").asInt());
            weatherData.put("visibility", root.path("visibility").asDouble() / 1000); // Convert to km
            weatherData.put("weatherCondition", root.path("weather").get(0).path("main").asText());
            weatherData.put("description", root.path("weather").get(0).path("description").asText());
            weatherData.put("dataSource", "OpenWeatherMap");
            weatherData.put("timestamp", LocalDateTime.now());

            return weatherData;

        } catch (Exception e) {
            log.error("Error parsing OpenWeatherMap response", e);
            return new HashMap<>();
        }
    }

    private Map<String, Object> parseOpenWeatherMapForecastResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            Map<String, Object> forecastData = new HashMap<>();
            List<Map<String, Object>> forecasts = new ArrayList<>();

            JsonNode list = root.path("list");
            for (JsonNode item : list) {
                Map<String, Object> forecast = new HashMap<>();
                forecast.put("temperature", item.path("main").path("temp").asDouble());
                forecast.put("humidity", item.path("main").path("humidity").asDouble());
                forecast.put("pressure", item.path("main").path("pressure").asDouble());
                forecast.put("weatherCondition", item.path("weather").get(0).path("main").asText());
                forecast.put("dateTime", item.path("dt_txt").asText());
                forecasts.add(forecast);
            }

            forecastData.put("forecasts", forecasts);
            forecastData.put("dataSource", "OpenWeatherMap");
            forecastData.put("timestamp", LocalDateTime.now());

            return forecastData;

        } catch (Exception e) {
            log.error("Error parsing OpenWeatherMap forecast response", e);
            return new HashMap<>();
        }
    }

    private Map<String, Object> parseNASAResponse(String jsonResponse) {
        try {
            JsonNode root = objectMapper.readTree(jsonResponse);
            Map<String, Object> historicalData = new HashMap<>();

            JsonNode parameters = root.path("properties").path("parameter");
            historicalData.put("temperature", parameters.path("T2M"));
            historicalData.put("precipitation", parameters.path("PRECTOT"));
            historicalData.put("windSpeed", parameters.path("WS2M"));
            historicalData.put("humidity", parameters.path("RH2M"));
            historicalData.put("dataSource", "NASA POWER");
            historicalData.put("timestamp", LocalDateTime.now());

            return historicalData;

        } catch (Exception e) {
            log.error("Error parsing NASA response", e);
            return new HashMap<>();
        }
    }

    private Map<String, Object> aggregateResults(List<Map<String, Object>> results, String locationName) {
        if (results.size() == 1) {
            return results.get(0);
        }

        // Simple aggregation logic - can be enhanced with weighted averages
        Map<String, Object> aggregated = new HashMap<>();
        aggregated.put("location", locationName);
        aggregated.put("dataSource", "Aggregated");
        aggregated.put("sources", results.size());
        aggregated.put("timestamp", LocalDateTime.now());

        // For now, return the first available result
        if (!results.isEmpty()) {
            aggregated.putAll(results.get(0));
        }

        return aggregated;
    }

    private Location createLocationFromWeatherData(String locationName, Map<String, Object> weatherData) {
        // Create basic location - in real app would use geocoding
        Location location = new Location();
        location.setName(locationName);
        location.setLatitude(new BigDecimal("40.7128")); // Default to NYC coordinates
        location.setLongitude(new BigDecimal("-74.0060"));
        location.setCountry("Unknown");
        location.setTimezone("UTC");
        return location;
    }

    private WeatherDataEntity convertToWeatherEntity(Location location, Map<String, Object> weatherData) {
        WeatherDataEntity entity = new WeatherDataEntity();
        entity.setLocation(location);
        entity.setRecordedAt(LocalDateTime.now());
        entity.setDataSource((String) weatherData.get("dataSource"));

        if (weatherData.get("temperature") != null) {
            entity.setTemperature(new BigDecimal(weatherData.get("temperature").toString()));
        }
        if (weatherData.get("humidity") != null) {
            entity.setHumidity(new BigDecimal(weatherData.get("humidity").toString()));
        }
        if (weatherData.get("pressure") != null) {
            entity.setPressure(new BigDecimal(weatherData.get("pressure").toString()));
        }
        if (weatherData.get("windSpeed") != null) {
            entity.setWindSpeed(new BigDecimal(weatherData.get("windSpeed").toString()));
        }
        if (weatherData.get("windDirection") != null) {
            entity.setWindDirection((Integer) weatherData.get("windDirection"));
        }
        if (weatherData.get("weatherCondition") != null) {
            entity.setWeatherCondition((String) weatherData.get("weatherCondition"));
        }

        return entity;
    }

    // Mock data generators (for demo/fallback purposes)
    private Map<String, Object> generateMockCurrentWeather(String locationName, String source) {
        Map<String, Object> mock = new HashMap<>();
        Random random = new Random(locationName.hashCode());

        mock.put("location", locationName);
        mock.put("temperature", 15 + random.nextInt(20));
        mock.put("humidity", 40 + random.nextInt(40));
        mock.put("pressure", 1000 + random.nextInt(50));
        mock.put("windSpeed", random.nextInt(30));
        mock.put("windDirection", random.nextInt(360));
        mock.put("cloudCover", random.nextInt(100));
        mock.put("visibility", 5 + random.nextInt(15));
        mock.put("weatherCondition", getRandomWeatherCondition(random));
        mock.put("dataSource", source + " (Mock)");
        mock.put("timestamp", LocalDateTime.now());

        return mock;
    }

    private Map<String, Object> generateMockForecast(String locationName, int days, String source) {
        Map<String, Object> mock = new HashMap<>();
        List<Map<String, Object>> forecasts = new ArrayList<>();
        Random random = new Random(locationName.hashCode());

        for (int i = 0; i < days; i++) {
            Map<String, Object> dailyForecast = new HashMap<>();
            dailyForecast.put("date", LocalDateTime.now().plusDays(i));
            dailyForecast.put("temperature", 15 + random.nextInt(20));
            dailyForecast.put("humidity", 40 + random.nextInt(40));
            dailyForecast.put("weatherCondition", getRandomWeatherCondition(random));
            forecasts.add(dailyForecast);
        }

        mock.put("location", locationName);
        mock.put("forecasts", forecasts);
        mock.put("dataSource", source + " (Mock)");
        mock.put("timestamp", LocalDateTime.now());

        return mock;
    }

    private Map<String, Object> generateMockHistoricalData(BigDecimal latitude, BigDecimal longitude,
            String startDate, String endDate, String source) {
        Map<String, Object> mock = new HashMap<>();
        mock.put("latitude", latitude);
        mock.put("longitude", longitude);
        mock.put("startDate", startDate);
        mock.put("endDate", endDate);
        mock.put("dataSource", source + " (Mock)");
        mock.put("timestamp", LocalDateTime.now());

        // Add mock historical data
        Map<String, Object> summary = new HashMap<>();
        summary.put("averageTemperature", 20.5);
        summary.put("totalPrecipitation", 45.2);
        summary.put("averageHumidity", 65);
        mock.put("summary", summary);

        return mock;
    }

    private String getRandomWeatherCondition(Random random) {
        String[] conditions = { "Clear", "Clouds", "Rain", "Snow", "Fog", "Drizzle", "Thunderstorm" };
        return conditions[random.nextInt(conditions.length)];
    }

    /**
     * Get current weather data for coordinates (no Redis, always live)
     */
    public Map<String, Object> getCurrentWeather(BigDecimal latitude, BigDecimal longitude) {
        try {
            String url = String.format(
                    "%s?lat=%s&lon=%s&appid=%s&units=metric",
                    OPENWEATHERMAP_CURRENT_URL, latitude, longitude, openWeatherMapApiKey);

            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> processedData = processOpenWeatherMapCurrentData(response.getBody());
                return processedData;
            }

        } catch (Exception e) {
            log.error("Error fetching current weather from OpenWeatherMap: {}", e.getMessage());
        }

        return getMockCurrentWeatherData();
    }

    /**
     * Get weather forecast for coordinates (no Redis, always live)
     */
    public Map<String, Object> getWeatherForecast(BigDecimal latitude, BigDecimal longitude, int days) {
        try {
            String url = String.format(
                    "%s?lat=%s&lon=%s&appid=%s&units=metric&cnt=%d",
                    OPENWEATHERMAP_FORECAST_URL, latitude, longitude, openWeatherMapApiKey, days * 8);

            @SuppressWarnings("rawtypes")
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map<String, Object> processedData = processOpenWeatherMapForecastData(response.getBody());
                return processedData;
            }

        } catch (Exception e) {
            log.error("Error fetching weather forecast from OpenWeatherMap: {}", e.getMessage());
        }

        return getMockForecastData(days);
    }

    // Helper methods
    private Map<String, Object> processOpenWeatherMapCurrentData(Map<String, Object> rawData) {
        Map<String, Object> processed = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> main = (Map<String, Object>) rawData.get("main");
            @SuppressWarnings("unchecked")
            Map<String, Object> wind = (Map<String, Object>) rawData.get("wind");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> weather = (List<Map<String, Object>>) rawData.get("weather");

            if (main != null) {
                processed.put("temperature", main.get("temp"));
                processed.put("humidity", main.get("humidity"));
                processed.put("pressure", main.get("pressure"));
            }

            if (wind != null) {
                processed.put("windSpeed", wind.get("speed"));
                processed.put("windDirection", wind.get("deg"));
            }

            if (weather != null && !weather.isEmpty()) {
                processed.put("weatherCondition", weather.get(0).get("main"));
            }

            processed.put("dataSource", "OpenWeatherMap");
            processed.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error processing OpenWeatherMap current data", e);
        }

        return processed;
    }

    private Map<String, Object> processOpenWeatherMapForecastData(Map<String, Object> rawData) {
        Map<String, Object> processed = new HashMap<>();

        try {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> list = (List<Map<String, Object>>) rawData.get("list");

            List<Map<String, Object>> forecasts = new ArrayList<>();

            if (list != null) {
                for (Map<String, Object> item : list) {
                    Map<String, Object> forecast = new HashMap<>();

                    @SuppressWarnings("unchecked")
                    Map<String, Object> main = (Map<String, Object>) item.get("main");
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> weather = (List<Map<String, Object>>) item.get("weather");

                    if (main != null) {
                        forecast.put("temperature", main.get("temp"));
                        forecast.put("humidity", main.get("humidity"));
                    }

                    if (weather != null && !weather.isEmpty()) {
                        forecast.put("weatherCondition", weather.get(0).get("main"));
                    }

                    forecast.put("datetime", item.get("dt_txt"));
                    forecasts.add(forecast);
                }
            }

            processed.put("forecasts", forecasts);
            processed.put("dataSource", "OpenWeatherMap");
            processed.put("timestamp", LocalDateTime.now());

        } catch (Exception e) {
            log.error("Error processing OpenWeatherMap forecast data", e);
        }

        return processed;
    }


    private Map<String, Object> getMockCurrentWeatherData() {
        Map<String, Object> mock = new HashMap<>();
        Random random = new Random();

        mock.put("temperature", 15 + random.nextInt(20));
        mock.put("humidity", 40 + random.nextInt(40));
        mock.put("pressure", 1000 + random.nextInt(50));
        mock.put("windSpeed", random.nextInt(30));
        mock.put("windDirection", random.nextInt(360));
        mock.put("weatherCondition", getRandomWeatherCondition(random));
        mock.put("dataSource", "Mock Data");
        mock.put("timestamp", LocalDateTime.now());

        return mock;
    }

    private Map<String, Object> getMockForecastData(int days) {
        Map<String, Object> mock = new HashMap<>();
        List<Map<String, Object>> forecasts = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < days; i++) {
            Map<String, Object> dailyForecast = new HashMap<>();
            dailyForecast.put("date", LocalDateTime.now().plusDays(i));
            dailyForecast.put("temperature", 15 + random.nextInt(20));
            dailyForecast.put("humidity", 40 + random.nextInt(40));
            dailyForecast.put("weatherCondition", getRandomWeatherCondition(random));
            forecasts.add(dailyForecast);
        }

        mock.put("forecasts", forecasts);
        mock.put("dataSource", "Mock Data");
        mock.put("timestamp", LocalDateTime.now());

        return mock;
    }

    /**
     * Get real weather data for a city name (simplified method for random weather endpoint)
     */
    public Map<String, Object> getRealWeatherDataForCity(String cityName) {
        try {
            log.info("Fetching real weather data for city: {}", cityName);
            CompletableFuture<Map<String, Object>> future = fetchCurrentWeatherFromOpenWeatherMap(cityName);
            Map<String, Object> rawWeatherData = future.get(apiTimeout, TimeUnit.MILLISECONDS);
            if (rawWeatherData != null && !rawWeatherData.isEmpty()) {
                // If the response is already parsed, return as is; otherwise, parse JSON string
                if (rawWeatherData.containsKey("main") && rawWeatherData.containsKey("weather")) {
                    // This is a raw OpenWeatherMap response, so convert it to JSON and parse
                    String jsonString = objectMapper.writeValueAsString(rawWeatherData);
                    Map<String, Object> parsed = parseOpenWeatherMapResponse(jsonString);
                    log.info("Successfully parsed real weather data for: {}", cityName);
                    return parsed;
                } else {
                    // Already parsed/flat map
                    log.info("Successfully fetched real weather data for: {}", cityName);
                    return rawWeatherData;
                }
            } else {
                log.warn("No weather data returned for: {}", cityName);
                return null;
            }

        } catch (Exception e) {
            log.error("Error fetching real weather data for city: {}", cityName, e);
            return null;
        }
    }
}
