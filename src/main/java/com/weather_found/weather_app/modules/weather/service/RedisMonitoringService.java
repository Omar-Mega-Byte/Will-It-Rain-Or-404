package com.weather_found.weather_app.modules.weather.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis monitoring service for cache health checks
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RedisMonitoringService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisConnectionFactory connectionFactory;

    /**
     * Check Redis health and connectivity
     */
    public Map<String, Object> checkHealth() {
        Map<String, Object> health = new HashMap<>();
        
        try {
            // Test Redis connectivity
            String testKey = "health:check:" + System.currentTimeMillis();
            String testValue = "health-check-value";
            
            // Set a test value
            redisTemplate.opsForValue().set(testKey, testValue);
            
            // Get the test value back
            Object retrievedValue = redisTemplate.opsForValue().get(testKey);
            
            // Clean up test key
            redisTemplate.delete(testKey);
            
            boolean isConnected = testValue.equals(retrievedValue);
            
            health.put("status", isConnected ? "UP" : "DOWN");
            health.put("connectivity", "CONNECTED");
            health.put("testResult", isConnected ? "PASSED" : "FAILED");
            health.put("timestamp", LocalDateTime.now());
            health.put("connectionFactory", connectionFactory.getClass().getSimpleName());
            
            if (!isConnected) {
                health.put("error", "Test value mismatch");
                health.put("expected", testValue);
                health.put("actual", retrievedValue);
            }
            
            // Try to get some connection info
            try {
                var connection = redisTemplate.getConnectionFactory().getConnection();
                health.put("connected", !connection.isClosed());
                connection.close();
            } catch (Exception e) {
                health.put("connectionError", e.getMessage());
                log.debug("Could not get connection info", e);
            }
            
        } catch (Exception e) {
            log.error("Redis health check failed", e);
            
            health.put("status", "DOWN");
            health.put("connectivity", "FAILED");
            health.put("error", e.getMessage());
            health.put("errorType", e.getClass().getSimpleName());
            health.put("timestamp", LocalDateTime.now());
        }
        
        return health;
    }
    
    /**
     * Get Redis server info
     */
    public Map<String, Object> getServerInfo() {
        Map<String, Object> info = new HashMap<>();
        
        try {
            info.put("connectionFactory", connectionFactory.getClass().getSimpleName());
            info.put("redisTemplate", "configured");
            info.put("timestamp", LocalDateTime.now());
            
            // Additional server information could be added here
            info.put("status", "operational");
            
        } catch (Exception e) {
            log.error("Error getting Redis server info", e);
            info.put("error", e.getMessage());
            info.put("status", "error");
        }
        
        return info;
    }
}
