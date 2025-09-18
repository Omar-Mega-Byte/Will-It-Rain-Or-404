package com.weather_found.weather_app.modules.analytics.listener;

import com.weather_found.weather_app.modules.analytics.event.ApiUsageEvent;
import com.weather_found.weather_app.modules.analytics.event.UserActivityEvent;
import com.weather_found.weather_app.modules.analytics.service.ApiUsageAnalyticsService;
import com.weather_found.weather_app.modules.analytics.service.UserAnalyticsService;
import com.weather_found.weather_app.modules.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsEventListener Tests")
class AnalyticsEventListenerTest {

    @Mock
    private UserAnalyticsService userAnalyticsService;

    @Mock
    private ApiUsageAnalyticsService apiUsageAnalyticsService;

    @InjectMocks
    private AnalyticsEventListener eventListener;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
    }

    @Test
    @DisplayName("Should handle user activity event successfully")
    void shouldHandleUserActivityEventSuccessfully() {
        // Given
        UserActivityEvent event = new UserActivityEvent(
                testUser,
                "LOGIN",
                "USER",
                1L,
                "192.168.1.1",
                "Mozilla/5.0",
                "session123",
                "User logged in",
                null,
                Instant.now());

        // When
        eventListener.handleUserActivityEvent(event);

        // Then
        verify(userAnalyticsService).recordActivity(
                eq(testUser),
                eq("LOGIN"),
                eq("USER"),
                eq(1L),
                eq("192.168.1.1"),
                eq("Mozilla/5.0"),
                eq("session123"),
                eq("User logged in"),
                isNull());
    }

    @Test
    @DisplayName("Should handle API usage event successfully")
    void shouldHandleApiUsageEventSuccessfully() {
        // Given
        ApiUsageEvent event = new ApiUsageEvent(
                "/api/weather",
                "GET",
                testUser,
                200,
                150L,
                1024L,
                2048L,
                "192.168.1.1",
                "Mozilla/5.0",
                "session123",
                null,
                "location=London",
                Instant.now());

        // When
        eventListener.handleApiUsageEvent(event);

        // Then
        verify(apiUsageAnalyticsService).recordApiUsage(
                eq("/api/weather"),
                eq("GET"),
                eq(testUser),
                eq(200),
                eq(150L),
                eq(1024L),
                eq(2048L),
                eq("192.168.1.1"),
                eq("Mozilla/5.0"),
                eq("session123"),
                isNull(),
                eq("location=London"));
    }

    @Test
    @DisplayName("Should handle user activity event with null user gracefully")
    void shouldHandleUserActivityEventWithNullUserGracefully() {
        // Given
        UserActivityEvent event = new UserActivityEvent(
                null,
                "ANONYMOUS_VIEW",
                "PAGE",
                1L,
                "192.168.1.1",
                "Mozilla/5.0",
                "session123",
                "Anonymous page view",
                null,
                Instant.now());

        // When
        eventListener.handleUserActivityEvent(event);

        // Then - The event listener should handle the NPE gracefully and not call the
        // service
        verify(userAnalyticsService, never()).recordActivity(any(), any(), any(), any(), any(), any(), any(), any(),
                any());
    }

    @Test
    @DisplayName("Should handle API usage event with null user gracefully")
    void shouldHandleApiUsageEventWithNullUserGracefully() {
        // Given
        ApiUsageEvent event = new ApiUsageEvent(
                "/api/public",
                "GET",
                null,
                200,
                100L,
                null,
                null,
                "192.168.1.1",
                "Mozilla/5.0",
                "session123",
                null,
                null,
                Instant.now());

        // When
        eventListener.handleApiUsageEvent(event);

        // Then
        verify(apiUsageAnalyticsService).recordApiUsage(
                eq("/api/public"),
                eq("GET"),
                isNull(),
                eq(200),
                eq(100L),
                isNull(),
                isNull(),
                eq("192.168.1.1"),
                eq("Mozilla/5.0"),
                eq("session123"),
                isNull(),
                isNull());
    }

    @Test
    @DisplayName("Should handle service exceptions gracefully")
    void shouldHandleServiceExceptionsGracefully() {
        // Given
        UserActivityEvent event = new UserActivityEvent(
                testUser,
                "LOGIN",
                "USER",
                1L,
                "192.168.1.1",
                "Mozilla/5.0",
                "session123",
                "User logged in",
                null,
                Instant.now());

        doThrow(new RuntimeException("Database error"))
                .when(userAnalyticsService)
                .recordActivity(any(), any(), any(), any(), any(), any(), any(), any(), any());

        // When & Then - Should not throw exception
        eventListener.handleUserActivityEvent(event);

        verify(userAnalyticsService).recordActivity(any(), any(), any(), any(), any(), any(), any(), any(), any());
    }
}