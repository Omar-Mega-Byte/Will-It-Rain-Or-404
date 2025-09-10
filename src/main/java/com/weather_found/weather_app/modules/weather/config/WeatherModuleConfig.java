package com.weather_found.weather_app.modules.weather.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.Executor;

/**
 * Weather module configuration
 */
@Configuration
@EnableAsync
@EnableScheduling
public class WeatherModuleConfig {

    @Value("${weather.async.core-pool-size:10}")
    private int asyncCorePoolSize;

    @Value("${weather.async.max-pool-size:20}")
    private int asyncMaxPoolSize;

    @Value("${weather.async.queue-capacity:500}")
    private int asyncQueueCapacity;

    @Value("${weather.redis.cache-ttl:300}")
    private int redisCacheTtl;

    /**
     * Redis template configuration for weather data
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> weatherRedisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Create ObjectMapper with JavaTimeModule for LocalDateTime serialization
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.findAndRegisterModules();

        // Use String serializer for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serializer for values
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }

    /**
     * RestTemplate for external API calls
     */
    @Bean
    public RestTemplate weatherRestTemplate() {
        return new RestTemplate();
    }

    /**
     * ObjectMapper for JSON processing
     */
    @Bean
    @Primary
    public ObjectMapper weatherObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.findAndRegisterModules();
        return mapper;
    }

    /**
     * Async executor for weather operations
     */
    @Bean(name = "weatherAsyncExecutor")
    public Executor weatherAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(asyncCorePoolSize);
        executor.setMaxPoolSize(asyncMaxPoolSize);
        executor.setQueueCapacity(asyncQueueCapacity);
        executor.setThreadNamePrefix("WeatherAsync-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.initialize();
        return executor;
    }

    /**
     * Weather module properties
     */
    @Bean
    public WeatherModuleProperties weatherModuleProperties() {
        return new WeatherModuleProperties();
    }

    /**
     * Weather module properties class
     */
    public static class WeatherModuleProperties {

        // Cache configuration
        private int currentWeatherCacheTtl = 300; // 5 minutes
        private int forecastCacheTtl = 1800; // 30 minutes
        private int historicalCacheTtl = 7200; // 2 hours
        private int locationCacheTtl = 3600; // 1 hour
        private int alertCacheTtl = 300; // 5 minutes

        // Rate limiting
        private int defaultRateLimit = 60; // requests per minute
        private int dashboardRateLimit = 30;
        private int searchRateLimit = 20;
        private int apiRateLimit = 100;

        // External API configuration
        private int apiTimeoutMs = 5000;
        private int maxRetries = 3;
        private int retryDelayMs = 1000;

        // Data validation
        private int maxLocationNameLength = 100;
        private int maxAlertTitleLength = 200;
        private int maxAlertDescriptionLength = 1000;
        private int maxSearchQueryLength = 100;

        // Security
        private boolean enableRateLimiting = true;
        private boolean enableInputValidation = true;
        private boolean enableSuspiciousActivityLogging = true;
        private int maxBlockDurationMinutes = 60;

        // Cleanup configuration
        private int dataRetentionDays = 365;
        private int alertRetentionDays = 30;
        private int logRetentionDays = 7;

        // Getters and setters
        public int getCurrentWeatherCacheTtl() {
            return currentWeatherCacheTtl;
        }

        public void setCurrentWeatherCacheTtl(int currentWeatherCacheTtl) {
            this.currentWeatherCacheTtl = currentWeatherCacheTtl;
        }

        public int getForecastCacheTtl() {
            return forecastCacheTtl;
        }

        public void setForecastCacheTtl(int forecastCacheTtl) {
            this.forecastCacheTtl = forecastCacheTtl;
        }

        public int getHistoricalCacheTtl() {
            return historicalCacheTtl;
        }

        public void setHistoricalCacheTtl(int historicalCacheTtl) {
            this.historicalCacheTtl = historicalCacheTtl;
        }

        public int getLocationCacheTtl() {
            return locationCacheTtl;
        }

        public void setLocationCacheTtl(int locationCacheTtl) {
            this.locationCacheTtl = locationCacheTtl;
        }

        public int getAlertCacheTtl() {
            return alertCacheTtl;
        }

        public void setAlertCacheTtl(int alertCacheTtl) {
            this.alertCacheTtl = alertCacheTtl;
        }

        public int getDefaultRateLimit() {
            return defaultRateLimit;
        }

        public void setDefaultRateLimit(int defaultRateLimit) {
            this.defaultRateLimit = defaultRateLimit;
        }

        public int getDashboardRateLimit() {
            return dashboardRateLimit;
        }

        public void setDashboardRateLimit(int dashboardRateLimit) {
            this.dashboardRateLimit = dashboardRateLimit;
        }

        public int getSearchRateLimit() {
            return searchRateLimit;
        }

        public void setSearchRateLimit(int searchRateLimit) {
            this.searchRateLimit = searchRateLimit;
        }

        public int getApiRateLimit() {
            return apiRateLimit;
        }

        public void setApiRateLimit(int apiRateLimit) {
            this.apiRateLimit = apiRateLimit;
        }

        public int getApiTimeoutMs() {
            return apiTimeoutMs;
        }

        public void setApiTimeoutMs(int apiTimeoutMs) {
            this.apiTimeoutMs = apiTimeoutMs;
        }

        public int getMaxRetries() {
            return maxRetries;
        }

        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }

        public int getRetryDelayMs() {
            return retryDelayMs;
        }

        public void setRetryDelayMs(int retryDelayMs) {
            this.retryDelayMs = retryDelayMs;
        }

        public int getMaxLocationNameLength() {
            return maxLocationNameLength;
        }

        public void setMaxLocationNameLength(int maxLocationNameLength) {
            this.maxLocationNameLength = maxLocationNameLength;
        }

        public int getMaxAlertTitleLength() {
            return maxAlertTitleLength;
        }

        public void setMaxAlertTitleLength(int maxAlertTitleLength) {
            this.maxAlertTitleLength = maxAlertTitleLength;
        }

        public int getMaxAlertDescriptionLength() {
            return maxAlertDescriptionLength;
        }

        public void setMaxAlertDescriptionLength(int maxAlertDescriptionLength) {
            this.maxAlertDescriptionLength = maxAlertDescriptionLength;
        }

        public int getMaxSearchQueryLength() {
            return maxSearchQueryLength;
        }

        public void setMaxSearchQueryLength(int maxSearchQueryLength) {
            this.maxSearchQueryLength = maxSearchQueryLength;
        }

        public boolean isEnableRateLimiting() {
            return enableRateLimiting;
        }

        public void setEnableRateLimiting(boolean enableRateLimiting) {
            this.enableRateLimiting = enableRateLimiting;
        }

        public boolean isEnableInputValidation() {
            return enableInputValidation;
        }

        public void setEnableInputValidation(boolean enableInputValidation) {
            this.enableInputValidation = enableInputValidation;
        }

        public boolean isEnableSuspiciousActivityLogging() {
            return enableSuspiciousActivityLogging;
        }

        public void setEnableSuspiciousActivityLogging(boolean enableSuspiciousActivityLogging) {
            this.enableSuspiciousActivityLogging = enableSuspiciousActivityLogging;
        }

        public int getMaxBlockDurationMinutes() {
            return maxBlockDurationMinutes;
        }

        public void setMaxBlockDurationMinutes(int maxBlockDurationMinutes) {
            this.maxBlockDurationMinutes = maxBlockDurationMinutes;
        }

        public int getDataRetentionDays() {
            return dataRetentionDays;
        }

        public void setDataRetentionDays(int dataRetentionDays) {
            this.dataRetentionDays = dataRetentionDays;
        }

        public int getAlertRetentionDays() {
            return alertRetentionDays;
        }

        public void setAlertRetentionDays(int alertRetentionDays) {
            this.alertRetentionDays = alertRetentionDays;
        }

        public int getLogRetentionDays() {
            return logRetentionDays;
        }

        public void setLogRetentionDays(int logRetentionDays) {
            this.logRetentionDays = logRetentionDays;
        }
    }
}
