package com.weather_found.weather_app.modules.weather.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Service for event-specific weather predictions and caching
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WeatherEventService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final WeatherLocationService locationService;

    // Event Weather Cache Keys
    private static final String EVENT_WEATHER_KEY = "event:weather:";
    private static final String EVENT_RECOMMENDATIONS_KEY = "event:recommendations:";
    private static final String EVENT_RISK_ASSESSMENT_KEY = "event:risk:";
    private static final String WEATHER_SENSITIVITY_KEY = "weather:sensitivity:";

    /**
     * Get weather prediction for specific event
     */
    @Cacheable(value = "eventWeather", key = "#eventId")
    public Map<String, Object> getEventWeatherPrediction(Long eventId, String location,
            LocalDateTime eventDateTime, String eventType) {
        log.info("Getting weather prediction for event: {} at location: {}", eventId, location);

        String key = EVENT_WEATHER_KEY + eventId;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> cachedData = (Map<String, Object>) cached;
                return cachedData;
            } catch (ClassCastException e) {
                log.warn("Invalid cached data format for event: {}", eventId);
            }
        }

        Map<String, Object> prediction = generateEventWeatherPrediction(eventId, location, eventDateTime, eventType);

        // Cache for 1 hour for events more than 24 hours away, 15 minutes for sooner
        // events
        long hoursUntilEvent = java.time.Duration.between(LocalDateTime.now(), eventDateTime).toHours();
        long ttl = hoursUntilEvent > 24 ? 60 : 15;

        redisTemplate.opsForValue().set(key, prediction, ttl, TimeUnit.MINUTES);

        return prediction;
    }

    /**
     * Get weather-based recommendations for an event
     */
    @Cacheable(value = "eventRecommendations", key = "#eventId + ':' + #eventType")
    public Map<String, Object> getEventRecommendations(Long eventId, String location,
            LocalDateTime eventDateTime, String eventType) {
        log.info("Getting weather recommendations for event: {} type: {}", eventId, eventType);

        String key = EVENT_RECOMMENDATIONS_KEY + eventId + ":" + eventType;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> cachedData = (Map<String, Object>) cached;
                return cachedData;
            } catch (ClassCastException e) {
                log.warn("Invalid cached recommendations for event: {}", eventId);
            }
        }

        Map<String, Object> recommendations = generateEventRecommendations(eventId, location, eventDateTime, eventType);

        // Cache recommendations for 2 hours
        redisTemplate.opsForValue().set(key, recommendations, 2, TimeUnit.HOURS);

        return recommendations;
    }

    /**
     * Assess weather risk for event
     */
    public Map<String, Object> assessWeatherRisk(Long eventId, String location,
            LocalDateTime eventDateTime, String eventType, String sensitivity) {
        log.info("Assessing weather risk for event: {} with sensitivity: {}", eventId, sensitivity);

        String key = EVENT_RISK_ASSESSMENT_KEY + eventId + ":" + sensitivity;
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> cachedData = (Map<String, Object>) cached;
                return cachedData;
            } catch (ClassCastException e) {
                log.warn("Invalid cached risk assessment for event: {}", eventId);
            }
        }

        Map<String, Object> riskAssessment = generateWeatherRiskAssessment(eventId, location, eventDateTime, eventType,
                sensitivity);

        // Cache risk assessment for 30 minutes
        redisTemplate.opsForValue().set(key, riskAssessment, 30, TimeUnit.MINUTES);

        return riskAssessment;
    }

    /**
     * Get alternative date/time suggestions for weather-sensitive events
     */
    public Map<String, Object> getAlternativeDateSuggestions(String location, LocalDateTime originalDateTime,
            String eventType, int daysToCheck) {
        log.info("Getting alternative dates for location: {} around: {}", location, originalDateTime);

        Map<String, Object> suggestions = new HashMap<>();
        List<Map<String, Object>> alternatives = new ArrayList<>();

        // Check weather for the next few days
        for (int i = 1; i <= daysToCheck; i++) {
            LocalDateTime alternativeDate = originalDateTime.plusDays(i);
            Map<String, Object> weatherForecast = getWeatherForDateTime(location, alternativeDate);

            Map<String, Object> alternative = new HashMap<>();
            alternative.put("date", alternativeDate);
            alternative.put("weather", weatherForecast);
            alternative.put("suitabilityScore", calculateEventSuitability(weatherForecast, eventType));
            alternative.put("recommendation", generateDateRecommendation(weatherForecast, eventType));

            alternatives.add(alternative);
        }

        // Sort by suitability score
        alternatives
                .sort((a, b) -> Double.compare((Double) b.get("suitabilityScore"), (Double) a.get("suitabilityScore")));

        suggestions.put("originalDate", originalDateTime);
        suggestions.put("location", location);
        suggestions.put("eventType", eventType);
        suggestions.put("alternatives", alternatives.subList(0, Math.min(5, alternatives.size())));
        suggestions.put("generatedAt", LocalDateTime.now());

        return suggestions;
    }

    /**
     * Get weather sensitivity configuration for event types
     */
    public Map<String, Object> getWeatherSensitivityConfig(String eventType) {
        String key = WEATHER_SENSITIVITY_KEY + eventType.toLowerCase();
        Object cached = redisTemplate.opsForValue().get(key);

        if (cached != null) {
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> cachedData = (Map<String, Object>) cached;
                return cachedData;
            } catch (ClassCastException e) {
                log.warn("Invalid cached sensitivity config for event type: {}", eventType);
            }
        }

        Map<String, Object> config = generateSensitivityConfig(eventType);

        // Cache sensitivity config for 24 hours (rarely changes)
        redisTemplate.opsForValue().set(key, config, 24, TimeUnit.HOURS);

        return config;
    }

    /**
     * Track event weather accuracy after the event
     */
    public void trackPredictionAccuracy(Long eventId, Map<String, Object> actualWeather) {
        log.info("Tracking prediction accuracy for event: {}", eventId);

        String predictionKey = EVENT_WEATHER_KEY + eventId;
        Object predictedWeather = redisTemplate.opsForValue().get(predictionKey);

        if (predictedWeather != null) {
            Map<String, Object> accuracyData = new HashMap<>();
            accuracyData.put("eventId", eventId);
            accuracyData.put("predicted", predictedWeather);
            accuracyData.put("actual", actualWeather);
            accuracyData.put("accuracy", calculateAccuracy(predictedWeather, actualWeather));
            accuracyData.put("trackedAt", LocalDateTime.now());

            // Store accuracy data for analytics
            String accuracyKey = "analytics:prediction:accuracy:" + eventId;
            redisTemplate.opsForValue().set(accuracyKey, accuracyData, 30, TimeUnit.DAYS);
        }
    }

    // Helper methods for generating mock data
    private Map<String, Object> generateEventWeatherPrediction(Long eventId, String location,
            LocalDateTime eventDateTime, String eventType) {
        Map<String, Object> prediction = new HashMap<>();
        Map<String, Object> locationData = locationService.getLocationData(location);

        prediction.put("eventId", eventId);
        prediction.put("location", locationData);
        prediction.put("eventDateTime", eventDateTime);
        prediction.put("eventType", eventType);

        // Generate weather forecast for the event time
        Map<String, Object> weather = generateWeatherForecast(location, eventDateTime);
        prediction.put("weather", weather);

        // Add event-specific information
        prediction.put("suitabilityScore", calculateEventSuitability(weather, eventType));
        prediction.put("risks", identifyWeatherRisks(weather, eventType));
        prediction.put("recommendations", generateWeatherRecommendations(weather, eventType));
        prediction.put("confidence", calculatePredictionConfidence(eventDateTime));
        prediction.put("generatedAt", LocalDateTime.now());

        return prediction;
    }

    private Map<String, Object> generateEventRecommendations(Long eventId, String location,
            LocalDateTime eventDateTime, String eventType) {
        Map<String, Object> recommendations = new HashMap<>();
        Map<String, Object> weather = generateWeatherForecast(location, eventDateTime);

        recommendations.put("eventId", eventId);
        recommendations.put("eventType", eventType);
        recommendations.put("location", location);
        recommendations.put("eventDateTime", eventDateTime);

        List<String> generalRecommendations = generateGeneralRecommendations(weather, eventType);
        List<String> equipmentRecommendations = generateEquipmentRecommendations(weather, eventType);
        List<String> timingRecommendations = generateTimingRecommendations(weather, eventType);

        recommendations.put("general", generalRecommendations);
        recommendations.put("equipment", equipmentRecommendations);
        recommendations.put("timing", timingRecommendations);
        recommendations.put("overallAdvice", generateOverallAdvice(weather, eventType));
        recommendations.put("generatedAt", LocalDateTime.now());

        return recommendations;
    }

    private Map<String, Object> generateWeatherRiskAssessment(Long eventId, String location,
            LocalDateTime eventDateTime, String eventType, String sensitivity) {
        Map<String, Object> riskAssessment = new HashMap<>();
        Map<String, Object> weather = generateWeatherForecast(location, eventDateTime);

        riskAssessment.put("eventId", eventId);
        riskAssessment.put("riskLevel", calculateRiskLevel(weather, eventType, sensitivity));
        riskAssessment.put("riskScore", calculateRiskScore(weather, eventType, sensitivity));
        riskAssessment.put("riskFactors", identifyRiskFactors(weather, eventType));
        riskAssessment.put("mitigation", generateMitigationStrategies(weather, eventType));
        riskAssessment.put("contingencyPlans", generateContingencyPlans(weather, eventType));
        riskAssessment.put("monitoringAdvice", generateMonitoringAdvice(eventDateTime));
        riskAssessment.put("sensitivity", sensitivity);
        riskAssessment.put("assessedAt", LocalDateTime.now());

        return riskAssessment;
    }

    private Map<String, Object> generateWeatherForecast(String location, LocalDateTime dateTime) {
        Map<String, Object> weather = new HashMap<>();

        // Generate mock weather data
        Random random = new Random(location.hashCode() + dateTime.hashCode());

        weather.put("temperature", 15 + random.nextInt(20));
        weather.put("humidity", 40 + random.nextInt(40));
        weather.put("precipitation", random.nextDouble() * 100);
        weather.put("windSpeed", random.nextInt(30));
        weather.put("cloudCover", random.nextInt(100));
        weather.put("visibility", 5 + random.nextInt(15));
        weather.put("condition", generateWeatherCondition(random));
        weather.put("uvIndex", random.nextInt(11));
        weather.put("pressure", 1000 + random.nextInt(50));

        return weather;
    }

    private String generateWeatherCondition(Random random) {
        String[] conditions = { "Sunny", "Partly Cloudy", "Cloudy", "Light Rain", "Heavy Rain",
                "Thunderstorm", "Snow", "Fog", "Windy", "Clear" };
        return conditions[random.nextInt(conditions.length)];
    }

    private double calculateEventSuitability(Map<String, Object> weather, String eventType) {
        // Mock suitability calculation based on weather and event type
        double score = 70; // Base score

        String condition = (String) weather.get("condition");
        Integer precipitation = (Integer) weather.get("precipitation");
        Integer windSpeed = (Integer) weather.get("windSpeed");

        // Adjust score based on weather conditions
        if ("Sunny".equals(condition) || "Clear".equals(condition)) {
            score += 20;
        } else if ("Heavy Rain".equals(condition) || "Thunderstorm".equals(condition)) {
            score -= 40;
        }

        if (precipitation > 50)
            score -= 20;
        if (windSpeed > 25)
            score -= 15;

        // Adjust for event type
        switch (eventType.toLowerCase()) {
            case "outdoor wedding", "picnic", "sports" -> {
                if (precipitation > 10)
                    score -= 30;
            }
            case "hiking", "camping" -> {
                if (windSpeed > 20)
                    score -= 20;
            }
        }

        return Math.max(0, Math.min(100, score));
    }

    private List<String> identifyWeatherRisks(Map<String, Object> weather, String eventType) {
        List<String> risks = new ArrayList<>();

        Integer precipitation = (Integer) weather.get("precipitation");
        Integer windSpeed = (Integer) weather.get("windSpeed");
        String condition = (String) weather.get("condition");

        if (precipitation > 50) {
            risks.add("High probability of rain may disrupt outdoor activities");
        }

        if (windSpeed > 25) {
            risks.add("Strong winds may affect setup and safety");
        }

        if ("Thunderstorm".equals(condition)) {
            risks.add("Lightning risk - consider moving indoors");
        }

        return risks;
    }

    private List<String> generateWeatherRecommendations(Map<String, Object> weather, String eventType) {
        List<String> recommendations = new ArrayList<>();

        Integer precipitation = (Integer) weather.get("precipitation");
        String condition = (String) weather.get("condition");

        if (precipitation > 30) {
            recommendations.add("Consider having a backup indoor venue");
            recommendations.add("Provide umbrellas or covered areas");
        }

        if ("Sunny".equals(condition)) {
            recommendations.add("Provide shade and sunscreen");
            recommendations.add("Ensure adequate hydration stations");
        }

        return recommendations;
    }

    // Additional helper methods...
    private Map<String, Object> getWeatherForDateTime(String location, LocalDateTime dateTime) {
        return generateWeatherForecast(location, dateTime);
    }

    private String generateDateRecommendation(Map<String, Object> weather, String eventType) {
        double score = calculateEventSuitability(weather, eventType);

        if (score > 80)
            return "Excellent weather conditions for your event";
        if (score > 60)
            return "Good weather, minor precautions recommended";
        if (score > 40)
            return "Fair weather, some planning adjustments needed";
        return "Poor weather conditions, consider rescheduling";
    }

    private Map<String, Object> generateSensitivityConfig(String eventType) {
        Map<String, Object> config = new HashMap<>();

        config.put("eventType", eventType);
        config.put("precipitationThreshold", getThresholdForEventType(eventType, "precipitation"));
        config.put("windSpeedThreshold", getThresholdForEventType(eventType, "windSpeed"));
        config.put("temperatureRange", getThresholdForEventType(eventType, "temperature"));
        config.put("criticalFactors", getCriticalFactorsForEventType(eventType));

        return config;
    }

    private Object getThresholdForEventType(String eventType, String factor) {
        // Mock threshold values
        return switch (factor) {
            case "precipitation" -> 20;
            case "windSpeed" -> 25;
            case "temperature" -> Map.of("min", 10, "max", 35);
            default -> null;
        };
    }

    private List<String> getCriticalFactorsForEventType(String eventType) {
        return switch (eventType.toLowerCase()) {
            case "outdoor wedding" -> List.of("precipitation", "windSpeed", "temperature");
            case "picnic" -> List.of("precipitation", "temperature");
            case "hiking" -> List.of("visibility", "windSpeed", "temperature");
            case "sports" -> List.of("precipitation", "windSpeed");
            default -> List.of("precipitation", "temperature");
        };
    }

    private String calculateRiskLevel(Map<String, Object> weather, String eventType, String sensitivity) {
        double riskScore = calculateRiskScore(weather, eventType, sensitivity);

        if (riskScore < 30)
            return "LOW";
        if (riskScore < 60)
            return "MEDIUM";
        if (riskScore < 80)
            return "HIGH";
        return "CRITICAL";
    }

    private double calculateRiskScore(Map<String, Object> weather, String eventType, String sensitivity) {
        // Mock risk calculation
        double baseRisk = 20;

        Integer precipitation = (Integer) weather.get("precipitation");
        Integer windSpeed = (Integer) weather.get("windSpeed");

        if (precipitation > 50)
            baseRisk += 30;
        if (windSpeed > 25)
            baseRisk += 25;

        // Adjust for sensitivity
        switch (sensitivity.toLowerCase()) {
            case "high" -> baseRisk *= 1.5;
            case "low" -> baseRisk *= 0.7;
        }

        return Math.min(100, baseRisk);
    }

    private List<String> identifyRiskFactors(Map<String, Object> weather, String eventType) {
        List<String> factors = new ArrayList<>();
        factors.add("Precipitation probability");
        factors.add("Wind conditions");
        factors.add("Temperature extremes");
        return factors;
    }

    private List<String> generateMitigationStrategies(Map<String, Object> weather, String eventType) {
        List<String> strategies = new ArrayList<>();
        strategies.add("Monitor weather updates hourly");
        strategies.add("Prepare backup plans");
        strategies.add("Have emergency contacts ready");
        return strategies;
    }

    private List<String> generateContingencyPlans(Map<String, Object> weather, String eventType) {
        List<String> plans = new ArrayList<>();
        plans.add("Indoor alternative venue");
        plans.add("Postponement procedures");
        plans.add("Guest notification system");
        return plans;
    }

    private List<String> generateMonitoringAdvice(LocalDateTime eventDateTime) {
        List<String> advice = new ArrayList<>();
        advice.add("Check weather 24 hours before event");
        advice.add("Monitor conditions 6 hours prior");
        advice.add("Final check 2 hours before start time");
        return advice;
    }

    private double calculatePredictionConfidence(LocalDateTime eventDateTime) {
        long hoursUntilEvent = java.time.Duration.between(LocalDateTime.now(), eventDateTime).toHours();

        if (hoursUntilEvent <= 24)
            return 0.9;
        if (hoursUntilEvent <= 72)
            return 0.75;
        if (hoursUntilEvent <= 168)
            return 0.6; // 1 week
        return 0.4;
    }

    private List<String> generateGeneralRecommendations(Map<String, Object> weather, String eventType) {
        return List.of("Check weather updates regularly", "Have contingency plans ready");
    }

    private List<String> generateEquipmentRecommendations(Map<String, Object> weather, String eventType) {
        return List.of("Bring weather-appropriate gear", "Consider shelter options");
    }

    private List<String> generateTimingRecommendations(Map<String, Object> weather, String eventType) {
        return List.of("Consider alternative timing", "Monitor weather patterns");
    }

    private String generateOverallAdvice(Map<String, Object> weather, String eventType) {
        return "Weather conditions require careful monitoring and preparation";
    }

    private double calculateAccuracy(Object predicted, Map<String, Object> actual) {
        // Mock accuracy calculation
        return 0.85; // 85% accuracy
    }
}
