package com.weather_found.weather_app.modules.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Location service integrated with weather caching
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherLocationService {

    private final RedisTemplate<String, Object> redisTemplate;

    // Location Cache Keys
    private static final String LOCATION_CACHE_KEY = "location:data:";
    private static final String GEOCODING_CACHE_KEY = "location:geocoding:";
    private static final String POPULAR_LOCATIONS_KEY = "weather:popular:locations";
    private static final String USER_LOCATIONS_KEY = "user:locations:";

    /**
     * Get location information with caching
     */
    @Cacheable(value = "locationData", key = "#locationName")
    public Map<String, Object> getLocationData(String locationName) {
        log.info("Fetching location data for: {}", locationName);

        String key = LOCATION_CACHE_KEY + locationName.toLowerCase();
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            return (Map<String, Object>) cached;
        }

        // Generate mock location data (in real app, would call geocoding API)
        Map<String, Object> locationData = generateLocationData(locationName);

        // Cache for 24 hours (location data doesn't change often)
        redisTemplate.opsForValue().set(key, locationData, 24, TimeUnit.HOURS);

        return locationData;
    }

    /**
     * Search locations with autocomplete
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
     * Get coordinates for a location
     */
    @Cacheable(value = "geocoding", key = "#locationName")
    public Map<String, Double> getCoordinates(String locationName) {
        String key = GEOCODING_CACHE_KEY + locationName.toLowerCase();
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            return (Map<String, Double>) cached;
        }

        // Mock coordinates (in real app, would use geocoding service)
        Map<String, Double> coordinates = generateCoordinates(locationName);

        // Cache coordinates for 7 days
        redisTemplate.opsForValue().set(key, coordinates, 7, TimeUnit.DAYS);

        return coordinates;
    }

    /**
     * Add user favorite location
     */
    public void addUserFavoriteLocation(String userId, String locationName) {
        String key = USER_LOCATIONS_KEY + userId;
        redisTemplate.opsForList().leftPush(key, locationName);
        redisTemplate.expire(key, 30, TimeUnit.DAYS);
        log.info("Added favorite location '{}' for user: {}", locationName, userId);
    }

    /**
     * Remove user favorite location
     */
    public void removeUserFavoriteLocation(String userId, String locationName) {
        String key = USER_LOCATIONS_KEY + userId;
        redisTemplate.opsForList().remove(key, 1, locationName);
        log.info("Removed favorite location '{}' for user: {}", locationName, userId);
    }

    /**
     * Get user favorite locations
     */
    public List<String> getUserFavoriteLocations(String userId) {
        String key = USER_LOCATIONS_KEY + userId;
        List<Object> locations = redisTemplate.opsForList().range(key, 0, -1);

        if (locations == null) {
            return new ArrayList<>();
        }

        return locations.stream()
                .map(Object::toString)
                .toList();
    }

    /**
     * Get popular locations globally
     */
    public Set<Object> getPopularLocations(int limit) {
        return redisTemplate.opsForZSet().reverseRange(POPULAR_LOCATIONS_KEY, 0, limit - 1);
    }

    /**
     * Track location usage
     */
    public void trackLocationUsage(String locationName) {
        redisTemplate.opsForZSet().incrementScore(POPULAR_LOCATIONS_KEY, locationName.toLowerCase(), 1);
        log.debug("Tracked usage for location: {}", locationName);
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

    // Helper methods for mock data generation
    private Map<String, Object> generateLocationData(String locationName) {
        Map<String, Double> coords = generateCoordinates(locationName);

        Map<String, Object> data = new HashMap<>();
        data.put("name", locationName);
        data.put("coordinates", coords);
        data.put("country", getCountryForLocation(locationName));
        data.put("timezone", getTimezoneForLocation(locationName));
        data.put("elevation", getElevation(locationName));
        data.put("population", getPopulation(locationName));
        data.put("type", "city");
        data.put("lastUpdated", LocalDateTime.now());

        return data;
    }

    private Map<String, Double> generateCoordinates(String locationName) {
        // Mock coordinates - in real app would use geocoding API
        Map<String, Double> coords = new HashMap<>();

        switch (locationName.toLowerCase()) {
            case "new york" -> {
                coords.put("latitude", 40.7128);
                coords.put("longitude", -74.0060);
            }
            case "london" -> {
                coords.put("latitude", 51.5074);
                coords.put("longitude", -0.1278);
            }
            case "tokyo" -> {
                coords.put("latitude", 35.6762);
                coords.put("longitude", 139.6503);
            }
            case "paris" -> {
                coords.put("latitude", 48.8566);
                coords.put("longitude", 2.3522);
            }
            default -> {
                // Generate pseudo-random coordinates for other locations
                int hash = locationName.hashCode();
                coords.put("latitude", 40.0 + (hash % 40));
                coords.put("longitude", -120.0 + (hash % 240));
            }
        }

        return coords;
    }

    private String getCountryForLocation(String locationName) {
        // Mock country mapping
        return switch (locationName.toLowerCase()) {
            case "new york" -> "United States";
            case "london" -> "United Kingdom";
            case "tokyo" -> "Japan";
            case "paris" -> "France";
            case "sydney" -> "Australia";
            case "toronto" -> "Canada";
            case "berlin" -> "Germany";
            case "mumbai" -> "India";
            case "singapore" -> "Singapore";
            case "dubai" -> "UAE";
            default -> "Unknown";
        };
    }

    private String getTimezoneForLocation(String locationName) {
        // Mock timezone mapping
        return switch (locationName.toLowerCase()) {
            case "new york" -> "America/New_York";
            case "london" -> "Europe/London";
            case "tokyo" -> "Asia/Tokyo";
            case "paris" -> "Europe/Paris";
            case "sydney" -> "Australia/Sydney";
            case "toronto" -> "America/Toronto";
            case "berlin" -> "Europe/Berlin";
            case "mumbai" -> "Asia/Kolkata";
            case "singapore" -> "Asia/Singapore";
            case "dubai" -> "Asia/Dubai";
            default -> "UTC";
        };
    }

    private int getElevation(String locationName) {
        // Mock elevation data
        return switch (locationName.toLowerCase()) {
            case "new york" -> 10;
            case "london" -> 35;
            case "tokyo" -> 40;
            case "paris" -> 35;
            case "sydney" -> 58;
            case "toronto" -> 76;
            case "berlin" -> 34;
            case "mumbai" -> 14;
            case "singapore" -> 15;
            case "dubai" -> 16;
            default -> 100;
        };
    }

    private int getPopulation(String locationName) {
        // Mock population data
        return switch (locationName.toLowerCase()) {
            case "new york" -> 8419000;
            case "london" -> 8982000;
            case "tokyo" -> 13960000;
            case "paris" -> 2161000;
            case "sydney" -> 5312000;
            case "toronto" -> 2731000;
            case "berlin" -> 3669000;
            case "mumbai" -> 20411000;
            case "singapore" -> 5454000;
            case "dubai" -> 3331000;
            default -> 500000;
        };
    }

    private String determineWeatherZone(Map<String, Double> coords) {
        double lat = coords.get("latitude");

        if (lat > 60)
            return "Arctic";
        if (lat > 30)
            return "Temperate";
        if (lat > -30)
            return "Subtropical";
        return "Antarctic";
    }

    private List<String> getNearbyWeatherStations(Map<String, Double> coords) {
        // Mock nearby weather stations
        List<String> stations = new ArrayList<>();
        stations.add("Station-" + coords.get("latitude").intValue());
        stations.add("Station-" + coords.get("longitude").intValue());
        return stations;
    }
}
