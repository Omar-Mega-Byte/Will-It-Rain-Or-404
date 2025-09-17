package com.weather_found.weather_app.modules.analytics.config;

import com.weather_found.weather_app.modules.analytics.interceptor.AnalyticsInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration for analytics module components
 */
@Configuration
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.analytics.enabled", havingValue = "true", matchIfMissing = false)
public class AnalyticsConfig implements WebMvcConfigurer {

    private final AnalyticsInterceptor analyticsInterceptor;

    @Override
    public void addInterceptors(@NonNull InterceptorRegistry registry) {
        registry.addInterceptor(analyticsInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/analytics/**", // Avoid tracking analytics endpoints themselves
                        "/actuator/**",
                        "/error/**",
                        "/swagger-ui/**",
                        "/v3/api-docs/**");
    }
}