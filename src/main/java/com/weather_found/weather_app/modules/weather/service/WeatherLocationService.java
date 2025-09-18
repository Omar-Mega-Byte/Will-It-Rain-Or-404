package com.weather_found.weather_app.modules.weather.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Location service without Redis caching
 */
@Service
@Slf4j
public class WeatherLocationService {

    /**
     * Get location information (mocked, no cache)
     */
    public Map<String, Object> getLocationData(String locationName) {
        log.info("Fetching location data for: {}", locationName);

        // Generate mock location data (in real app, would call geocoding API)
        return generateLocationData(locationName);
    }

    /**
     * Search locations with autocomplete (mocked)
     */
    public List<Map<String, Object>> searchLocations(String query, int limit) {
        log.info("Searching locations for query: {} with limit: {}", query, limit);

        List<Map<String, Object>> results = new ArrayList<>();

        // In real implementation, would call external geocoding API
        String[] mockCities = { "New York", "London", "Tokyo", "Paris", "Sydney",
                "Toronto", "Berlin", "Mumbai", "Singapore", "Dubai" };

        for (String city : mockCities) {
            if (city.toLowerCase().contains(query.toLowerCase()) && results.size() < limit) {
                results.add(generateLocationData(city));
            }
        }

        return results;
    }

    /**
     * Get coordinates for a location (mocked, no cache)
     */
    public Map<String, Double> getCoordinates(String locationName) {
        return generateCoordinates(locationName);
    }

    /**
     * Get location weather zones (for weather prediction optimization)
     */
    public Map<String, Object> getWeatherZoneInfo(String locationName) {
        Map<String, Double> coords = getCoordinates(locationName);

        Map<String, Object> zoneInfo = new HashMap<>();
        zoneInfo.put("location", locationName);
        zoneInfo.put("coordinates", coords);
        zoneInfo.put("timezone", getTimezoneForLocation(locationName));
        zoneInfo.put("weatherZone", determineWeatherZone(coords));
        zoneInfo.put("elevation", getElevation(locationName));
        zoneInfo.put("nearbyStations", getNearbyWeatherStations(coords));

        return zoneInfo;
    }

    /**
     * Validate location exists and is supported
     */
    public boolean validateLocation(String locationName) {
        try {
            Map<String, Double> coords = getCoordinates(locationName);
            return coords.containsKey("latitude") && coords.containsKey("longitude");
        } catch (Exception e) {
            log.warn("Location validation failed for: {}", locationName, e);
            return false;
        }
    }

    // Mock methods (implementations unchanged)
    private Map<String, Object> generateLocationData(String locationName) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", locationName);
        data.put("country", "MockCountry");
        data.put("lat", 0.0);
        data.put("lon", 0.0);

        return data;
    }

    private Map<String, Double> generateCoordinates(String locationName) {
        Map<String, Double> coords = new HashMap<>();
        coords.put("lat", 0.0);
        coords.put("lon", 0.0);

        return coords;
    }

    private String getTimezoneForLocation(String locationName) {
        return "UTC";
    }

    private String determineWeatherZone(Map<String, Double> coords) {
        return "Temperate";
    }

    private double getElevation(String locationName) {
        return 0.0;
    }

    private List<String> getNearbyWeatherStations(Map<String, Double> coords) {
        return List.of("Station1", "Station2");
    }
}
