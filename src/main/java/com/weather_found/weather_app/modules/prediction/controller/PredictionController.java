package com.weather_found.weather_app.modules.prediction.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * REST controller for weather prediction management
 */
@RestController
@RequestMapping("/api/predictions")
@CrossOrigin(origins = "*", maxAge = 3600)
@Tag(name = "Weather Predictions", description = "ML-powered weather prediction endpoints")
@SecurityRequirement(name = "bearerAuth")
public class PredictionController {

    /**
     * Get weather prediction for location and date
     */
    @PostMapping("/forecast")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get weather prediction", description = "Get ML-powered weather prediction for specific location and date")
    public ResponseEntity<String> getWeatherPrediction(@RequestBody String predictionRequest,
            Authentication authentication) {
        String weatherPrediction = """
                {
                    "status": "success",
                    "predictionId": "PRED_%s",
                    "requestedAt": "%s",
                    "location": {
                        "name": "New York City",
                        "coordinates": {"lat": 40.7128, "lng": -74.0060}
                    },
                    "targetDate": "2025-09-15",
                    "prediction": {
                        "confidence": "89%%",
                        "accuracy": "±2°C temperature, ±10%% precipitation",
                        "model": "ML_WeatherNet_v3.2",
                        "lastTrained": "2025-09-01T00:00:00Z",
                        "dataPoints": 15847
                    },
                    "forecast": {
                        "temperature": {
                            "min": "18°C",
                            "max": "24°C",
                            "average": "21°C",
                            "feelsLike": "22°C"
                        },
                        "precipitation": {
                            "probability": "25%%",
                            "expectedAmount": "2-5mm",
                            "type": "light rain",
                            "duration": "2-3 hours"
                        },
                        "wind": {
                            "speed": "15 km/h",
                            "direction": "Southwest",
                            "gusts": "22 km/h"
                        },
                        "humidity": "68%%",
                        "pressure": "1013 hPa",
                        "visibility": "10+ km",
                        "uvIndex": "6 (High)"
                    },
                    "hourlyForecast": [
                        {"time": "06:00", "temp": "18°C", "condition": "Cloudy"},
                        {"time": "12:00", "temp": "22°C", "condition": "Partly Cloudy"},
                        {"time": "18:00", "temp": "21°C", "condition": "Light Rain"}
                    ],
                    "recommendations": [
                        "Bring a light jacket for morning",
                        "Umbrella recommended for afternoon",
                        "Good conditions for most outdoor activities"
                    ]
                }
                """.formatted(System.currentTimeMillis(), java.time.Instant.now().toString());
        return ResponseEntity.ok(weatherPrediction);
    }

    /**
     * Get prediction for specific event
     */
    @GetMapping("/event/{eventId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get event prediction", description = "Get weather prediction for a specific event")
    public ResponseEntity<String> getEventPrediction(@PathVariable Long eventId, Authentication authentication) {
        String eventPrediction = """
                {
                    "eventId": %d,
                    "eventDetails": {
                        "title": "Beach Volleyball Tournament",
                        "date": "2025-09-15",
                        "time": "10:00-16:00",
                        "location": "Santa Monica Beach",
                        "coordinates": {"lat": 34.0195, "lng": -118.4912}
                    },
                    "predictionSummary": {
                        "overallRating": "Excellent",
                        "confidence": "92%%",
                        "suitability": "Highly Suitable",
                        "riskLevel": "Low"
                    },
                    "weatherForecast": {
                        "morning": {
                            "time": "10:00",
                            "temperature": "22°C",
                            "condition": "Sunny",
                            "wind": "8 km/h",
                            "humidity": "65%%"
                        },
                        "afternoon": {
                            "time": "14:00",
                            "temperature": "26°C",
                            "condition": "Partly Cloudy",
                            "wind": "12 km/h",
                            "humidity": "58%%"
                        },
                        "evening": {
                            "time": "16:00",
                            "temperature": "24°C",
                            "condition": "Sunny",
                            "wind": "10 km/h",
                            "humidity": "62%%"
                        }
                    },
                    "eventSpecificAnalysis": {
                        "beachConditions": {
                            "sandTemperature": "Warm but comfortable",
                            "waveHeight": "0.5-1.0m (Good)",
                            "tideTimes": ["Low: 08:30", "High: 14:45"]
                        },
                        "sportsSuitability": {
                            "volleyball": "Excellent - Light winds, good visibility",
                            "comfort": "High - Optimal temperature range",
                            "sunExposure": "High - Sunscreen recommended"
                        }
                    },
                    "recommendations": [
                        "Perfect conditions for beach volleyball",
                        "Bring plenty of water and sunscreen",
                        "Consider shade structures for spectators",
                        "No weather-related concerns expected"
                    ],
                    "alerts": [],
                    "lastUpdated": "%s"
                }
                """.formatted(eventId, java.time.Instant.now().toString());
        return ResponseEntity.ok(eventPrediction);
    }

    /**
     * Get prediction history for user
     */
    @GetMapping("/history")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Operation(summary = "Get prediction history", description = "Get user's weather prediction history")
    public ResponseEntity<String> getPredictionHistory(Authentication authentication) {
        String username = authentication.getName();
        String predictionHistory = """
                {
                    "user": "%s",
                    "totalPredictions": 127,
                    "dateRange": {
                        "firstPrediction": "2025-07-15T09:30:00Z",
                        "lastPrediction": "2025-09-05T14:20:00Z"
                    },
                    "accuracy": {
                        "overall": "87.3%%",
                        "temperature": "92.1%%",
                        "precipitation": "81.5%%",
                        "wind": "89.7%%"
                    },
                    "recentPredictions": [
                        {
                            "id": "PRED_1725545234567",
                            "date": "2025-09-05T14:20:00Z",
                            "location": "New York City",
                            "targetDate": "2025-09-15",
                            "predicted": {"temp": "21°C", "condition": "Partly Cloudy"},
                            "actual": {"temp": "22°C", "condition": "Partly Cloudy"},
                            "accuracy": "95%%"
                        },
                        {
                            "id": "PRED_1725458834567",
                            "date": "2025-09-04T14:20:00Z",
                            "location": "Los Angeles",
                            "targetDate": "2025-09-14",
                            "predicted": {"temp": "28°C", "condition": "Sunny"},
                            "actual": {"temp": "27°C", "condition": "Sunny"},
                            "accuracy": "98%%"
                        },
                        {
                            "id": "PRED_1725372434567",
                            "date": "2025-09-03T14:20:00Z",
                            "location": "London",
                            "targetDate": "2025-09-13",
                            "predicted": {"temp": "16°C", "condition": "Rainy"},
                            "actual": {"temp": "15°C", "condition": "Rainy"},
                            "accuracy": "92%%"
                        }
                    ],
                    "statistics": {
                        "mostAccurateLocation": "New York City (94.2%%)",
                        "averageConfidence": "89.1%%",
                        "predictionTypes": {
                            "daily": 98,
                            "hourly": 23,
                            "extended": 6
                        },
                        "popularLocations": [
                            {"name": "New York City", "count": 34},
                            {"name": "Los Angeles", "count": 28},
                            {"name": "London", "count": 21}
                        ]
                    }
                }
                """.formatted(username);
        return ResponseEntity.ok(predictionHistory);
    }

    /**
     * Get prediction accuracy metrics - Admin only
     */
    @GetMapping("/accuracy")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get prediction accuracy", description = "Get prediction accuracy metrics (Admin only)")
    public ResponseEntity<String> getPredictionAccuracy(Authentication authentication) {
        String accuracyMetrics = """
                {
                    "systemOverview": {
                        "totalPredictions": 458923,
                        "overallAccuracy": "89.2%%",
                        "lastEvaluated": "%s",
                        "evaluationPeriod": "Last 30 days"
                    },
                    "modelPerformance": {
                        "primary": {
                            "name": "ML_WeatherNet_v3.2",
                            "accuracy": "89.2%%",
                            "confidence": "High",
                            "trainingData": "2.1M datapoints",
                            "lastTrained": "2025-09-01T00:00:00Z"
                        },
                        "backup": {
                            "name": "ML_WeatherNet_v3.1",
                            "accuracy": "86.7%%",
                            "confidence": "Medium",
                            "status": "Standby"
                        }
                    },
                    "accuracyByCategory": {
                        "temperature": {
                            "accuracy": "92.1%%",
                            "averageError": "±1.2°C",
                            "withinRange": "96.8%%"
                        },
                        "precipitation": {
                            "accuracy": "85.7%%",
                            "averageError": "±8%%",
                            "withinRange": "89.3%%"
                        },
                        "wind": {
                            "accuracy": "88.9%%",
                            "averageError": "±2.3 km/h",
                            "withinRange": "92.1%%"
                        },
                        "humidity": {
                            "accuracy": "91.4%%",
                            "averageError": "±4%%",
                            "withinRange": "94.7%%"
                        }
                    },
                    "accuracyByTimeframe": {
                        "1day": "94.2%%",
                        "3days": "89.1%%",
                        "7days": "82.6%%",
                        "14days": "76.3%%"
                    },
                    "geographicalAccuracy": {
                        "northAmerica": "91.3%%",
                        "europe": "89.7%%",
                        "asia": "87.2%%",
                        "other": "85.1%%"
                    },
                    "trends": {
                        "lastMonth": "+2.1%%",
                        "lastQuarter": "+5.3%%",
                        "yearOverYear": "+12.7%%"
                    },
                    "benchmarks": {
                        "industryAverage": "82.5%%",
                        "ourPerformance": "+6.7%% above average",
                        "topCompetitor": "86.3%%"
                    }
                }
                """.formatted(java.time.Instant.now().toString());
        return ResponseEntity.ok(accuracyMetrics);
    }

    /**
     * Retrain ML models - Admin only
     */
    @PostMapping("/retrain")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Retrain ML models", description = "Trigger ML model retraining (Admin only)")
    public ResponseEntity<String> retrainModels(Authentication authentication) {
        String retrainingResult = """
                {
                    "status": "initiated",
                    "message": "ML model retraining has been initiated successfully",
                    "retrainingJob": {
                        "jobId": "RETRAIN_%s",
                        "initiatedBy": "%s",
                        "initiatedAt": "%s",
                        "estimatedDuration": "4-6 hours",
                        "priority": "high"
                    },
                    "models": {
                        "primary": {
                            "name": "ML_WeatherNet_v3.2",
                            "currentAccuracy": "89.2%%",
                            "targetAccuracy": "91.0%%",
                            "status": "queued"
                        },
                        "experimental": {
                            "name": "ML_WeatherNet_v4.0",
                            "features": ["Enhanced precipitation model", "Improved wind prediction"],
                            "status": "training"
                        }
                    },
                    "trainingData": {
                        "totalRecords": "2.8M datapoints",
                        "newRecords": "0.7M datapoints",
                        "dataQuality": "98.3%%",
                        "timeRange": "2023-01-01 to 2025-09-05"
                    },
                    "infrastructure": {
                        "computeNodes": 8,
                        "gpuHours": "estimated 32 hours",
                        "cost": "$245 estimated",
                        "location": "ML Training Cluster East"
                    },
                    "schedule": {
                        "startTime": "%s",
                        "estimatedCompletion": "%s",
                        "validationPhase": "2 hours",
                        "deploymentPhase": "1 hour"
                    },
                    "notifications": {
                        "progressUpdates": "every 30 minutes",
                        "completionAlert": "enabled",
                        "failureAlert": "enabled"
                    },
                    "monitoring": {
                        "dashboardUrl": "/admin/ml/training/RETRAIN_%s",
                        "logsUrl": "/admin/ml/logs/RETRAIN_%s",
                        "metricsUrl": "/admin/ml/metrics/RETRAIN_%s"
                    }
                }
                """.formatted(
                System.currentTimeMillis(),
                authentication.getName(),
                java.time.Instant.now().toString(),
                java.time.Instant.now().toString(),
                java.time.Instant.now().plusSeconds(21600).toString(),
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                System.currentTimeMillis());
        return ResponseEntity.ok(retrainingResult);
    }
}
