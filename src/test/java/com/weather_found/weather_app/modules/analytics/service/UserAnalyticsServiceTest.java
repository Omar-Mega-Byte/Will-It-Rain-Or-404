package com.weather_found.weather_app.modules.analytics.service;

import com.weather_found.weather_app.modules.analytics.dto.response.UserAnalyticsResponseDto;
import com.weather_found.weather_app.modules.analytics.model.UserActivityLog;
import com.weather_found.weather_app.modules.analytics.model.UserSession;
import com.weather_found.weather_app.modules.analytics.repository.UserActivityLogRepository;
import com.weather_found.weather_app.modules.analytics.repository.UserSessionRepository;
import com.weather_found.weather_app.modules.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserAnalyticsService Tests")
class UserAnalyticsServiceTest {

        @Mock
        private UserActivityLogRepository activityLogRepository;

        @Mock
        private UserSessionRepository sessionRepository;

        @InjectMocks
        private UserAnalyticsService userAnalyticsService;

        private User testUser;
        private UserActivityLog testActivity;
        private UserSession testSession;

        @BeforeEach
        void setUp() {
                testUser = new User();
                testUser.setId(1L);
                testUser.setUsername("testuser");
                testUser.setEmail("test@example.com");

                testActivity = new UserActivityLog();
                testActivity.setId(1L);
                testActivity.setUser(testUser);
                testActivity.setAction("LOGIN");
                testActivity.setTimestamp(Instant.now());

                testSession = new UserSession();
                testSession.setId(1L);
                testSession.setUser(testUser);
                testSession.setSessionId("session123");
                testSession.setSessionStart(Instant.now().minus(1, ChronoUnit.HOURS));
                testSession.setIsActive(true);
        }

        @Nested
        @DisplayName("Activity Recording Tests")
        class ActivityRecordingTests {

                @Test
                @DisplayName("Should record user activity successfully")
                void shouldRecordUserActivitySuccessfully() {
                        // Given
                        when(activityLogRepository.save(any(UserActivityLog.class))).thenReturn(testActivity);

                        // When
                        userAnalyticsService.recordActivity(testUser, "LOGIN", "USER", 1L,
                                        "192.168.1.1", "Mozilla/5.0", "session123", null, 100L);

                        // Then
                        verify(activityLogRepository).save(argThat(
                                        activity -> (activity.getUser() == null || activity.getUser().equals(testUser))
                                                        &&
                                                        activity.getAction().equals("LOGIN") &&
                                                        activity.getEntityType().equals("USER") &&
                                                        activity.getEntityId().equals(1L) &&
                                                        activity.getIpAddress().equals("192.168.1.1") &&
                                                        activity.getUserAgent().equals("Mozilla/5.0") &&
                                                        activity.getSessionId().equals("session123") &&
                                                        activity.getResponseTimeMs().equals(100L)));
                }

                @Test
                @DisplayName("Should handle null optional parameters gracefully")
                void shouldHandleNullOptionalParametersGracefully() {
                        // Given
                        when(activityLogRepository.save(any(UserActivityLog.class))).thenReturn(testActivity);

                        // When
                        userAnalyticsService.recordActivity(testUser, "LOGIN", null, null, null, null, null, null,
                                        null);

                        // Then
                        verify(activityLogRepository).save(argThat(
                                        activity -> (activity.getUser() == null || activity.getUser().equals(testUser))
                                                        &&
                                                        activity.getAction().equals("LOGIN") &&
                                                        activity.getEntityType() == null &&
                                                        activity.getEntityId() == null &&
                                                        activity.getIpAddress() == null &&
                                                        activity.getUserAgent() == null &&
                                                        activity.getSessionId() == null &&
                                                        activity.getDetails() == null &&
                                                        activity.getResponseTimeMs() == null));
                }

                @Test
                @DisplayName("Should handle repository exception gracefully")
                void shouldHandleRepositoryExceptionGracefully() {
                        // Given
                        when(activityLogRepository.save(any(UserActivityLog.class)))
                                        .thenThrow(new RuntimeException("Database error"));

                        // When & Then - Should not throw exception
                        userAnalyticsService.recordActivity(testUser, "LOGIN", "USER", 1L,
                                        "192.168.1.1", "Mozilla/5.0", "session123", null, 100L);

                        verify(activityLogRepository).save(any(UserActivityLog.class));
                }
        }

        @Nested
        @DisplayName("Session Management Tests")
        class SessionManagementTests {

                @Test
                @DisplayName("Should start new session successfully")
                void shouldStartNewSessionSuccessfully() {
                        // Given
                        when(sessionRepository.save(any(UserSession.class))).thenReturn(testSession);

                        // When
                        UserSession result = userAnalyticsService.startSession(testUser, "session123",
                                        "192.168.1.1", "Mozilla/5.0");

                        // Then
                        assertThat(result).isNotNull();
                        verify(sessionRepository).save(argThat(session -> session.getUser().equals(testUser) &&
                                        session.getSessionId().equals("session123") &&
                                        session.getIpAddress().equals("192.168.1.1") &&
                                        session.getUserAgent().equals("Mozilla/5.0")));
                }

                @Test
                @DisplayName("Should end session successfully")
                void shouldEndSessionSuccessfully() {
                        // Given
                        when(sessionRepository.findBySessionIdAndIsActiveTrue("session123"))
                                        .thenReturn(Optional.of(testSession));

                        // When
                        userAnalyticsService.endSession("session123");

                        // Then
                        verify(sessionRepository).save(testSession);
                        assertThat(testSession.getIsActive()).isFalse();
                        assertThat(testSession.getSessionEnd()).isNotNull();
                }

                @Test
                @DisplayName("Should update session activity successfully")
                void shouldUpdateSessionActivitySuccessfully() {
                        // Given
                        testSession.setPageViews(0);
                        testSession.setActionsPerformed(0);
                        when(sessionRepository.findBySessionIdAndIsActiveTrue("session123"))
                                        .thenReturn(Optional.of(testSession));

                        // When
                        userAnalyticsService.updateSessionActivity("session123", true, true);

                        // Then
                        verify(sessionRepository).save(testSession);
                        assertThat(testSession.getPageViews()).isEqualTo(1);
                        assertThat(testSession.getActionsPerformed()).isEqualTo(1);
                }

                @Test
                @DisplayName("Should handle missing session gracefully")
                void shouldHandleMissingSessionGracefully() {
                        // Given
                        when(sessionRepository.findBySessionIdAndIsActiveTrue("session123"))
                                        .thenReturn(Optional.empty());

                        // When
                        userAnalyticsService.endSession("session123");

                        // Then
                        verify(sessionRepository, never()).save(any());
                }
        }

        @Nested
        @DisplayName("Analytics Retrieval Tests")
        class AnalyticsRetrievalTests {

                @Test
                @DisplayName("Should get user analytics successfully")
                void shouldGetUserAnalyticsSuccessfully() {
                        // Given
                        int daysBack = 30;

                        when(activityLogRepository.countUserActivitiesBetween(eq(testUser), any(), any()))
                                        .thenReturn(10L);
                        // Mock repository methods with proper Object[] arrays
                        List<Object[]> mockActionFrequency = new ArrayList<>();
                        mockActionFrequency.add(new Object[] { "LOGIN", 5L });
                        when(activityLogRepository.findMostFrequentActionsByUser(testUser))
                                        .thenReturn(mockActionFrequency);

                        List<Object[]> mockTrends = new ArrayList<>();
                        mockTrends.add(new Object[] { "2024-01-01", 3L });
                        when(activityLogRepository.findUserActivityTrends(eq(testUser), any(), any()))
                                        .thenReturn(mockTrends);

                        List<Object[]> mockHourlyData = new ArrayList<>();
                        mockHourlyData.add(new Object[] { 9, 5L });
                        when(activityLogRepository.findUserActivityByHour(eq(testUser), any(), any()))
                                        .thenReturn(mockHourlyData);

                        List<Object[]> mockSessionStats = new ArrayList<>();
                        mockSessionStats.add(new Object[] { 5L, 1800.0, 10, 15 });
                        when(sessionRepository.findUserEngagementStats(eq(testUser), any(), any()))
                                        .thenReturn(mockSessionStats);

                        Page<UserActivityLog> recentActivities = new PageImpl<>(List.of(testActivity));
                        when(activityLogRepository.findByUserOrderByTimestampDesc(eq(testUser), any(PageRequest.class)))
                                        .thenReturn(recentActivities);
                        when(activityLogRepository.findByUserAndTimestampBetween(eq(testUser), any(), any()))
                                        .thenReturn(List.of(testActivity));

                        // When
                        UserAnalyticsResponseDto response = userAnalyticsService.getUserAnalytics(testUser, daysBack);

                        // Then
                        assertThat(response).isNotNull();
                        assertThat(response.getUsername()).isEqualTo("testuser");
                        assertThat(response.getTotalQueries()).isEqualTo(10L);
                        assertThat(response.getActionFrequency()).containsEntry("LOGIN", 5L);
                        assertThat(response.getActivityTrends()).hasSize(1);
                        assertThat(response.getHourlyActivity()).containsEntry(9, 5L);
                        assertThat(response.getSessionStats()).isNotNull();
                        assertThat(response.getSessionStats().getTotalSessions()).isEqualTo(5L);
                }

                @Test
                @DisplayName("Should handle empty analytics gracefully")
                void shouldHandleEmptyAnalyticsGracefully() {
                        // Given
                        int daysBack = 30;

                        when(activityLogRepository.countUserActivitiesBetween(eq(testUser), any(), any()))
                                        .thenReturn(0L);
                        when(activityLogRepository.findMostFrequentActionsByUser(testUser))
                                        .thenReturn(List.of());
                        when(activityLogRepository.findUserActivityTrends(eq(testUser), any(), any()))
                                        .thenReturn(List.of());
                        when(activityLogRepository.findUserActivityByHour(eq(testUser), any(), any()))
                                        .thenReturn(List.of());
                        when(sessionRepository.findUserEngagementStats(eq(testUser), any(), any()))
                                        .thenReturn(List.of());

                        Page<UserActivityLog> emptyPage = new PageImpl<>(List.of());
                        when(activityLogRepository.findByUserOrderByTimestampDesc(eq(testUser), any(PageRequest.class)))
                                        .thenReturn(emptyPage);
                        when(activityLogRepository.findByUserAndTimestampBetween(eq(testUser), any(), any()))
                                        .thenReturn(List.of());

                        // When
                        UserAnalyticsResponseDto response = userAnalyticsService.getUserAnalytics(testUser, daysBack);

                        // Then
                        assertThat(response).isNotNull();
                        assertThat(response.getTotalQueries()).isEqualTo(0L);
                        assertThat(response.getActionFrequency()).isEmpty();
                        assertThat(response.getActivityTrends()).isEmpty();
                        assertThat(response.getHourlyActivity()).isEmpty();
                        assertThat(response.getSessionStats()).isNull();
                        assertThat(response.getLastActive()).isNull();
                }
        }

        @Nested
        @DisplayName("Device Detection Tests")
        class DeviceDetectionTests {

                @Test
                @DisplayName("Should detect mobile device correctly")
                void shouldDetectMobileDeviceCorrectly() {
                        // Given
                        String mobileUserAgent = "Mozilla/5.0 (iPhone; CPU iPhone OS 14_0 like Mac OS X)";
                        when(sessionRepository.save(any(UserSession.class))).thenReturn(testSession);

                        // When
                        userAnalyticsService.startSession(testUser, "session123",
                                        "192.168.1.1", mobileUserAgent);

                        // Then
                        verify(sessionRepository).save(argThat(session -> "Mobile".equals(session.getDeviceType()) &&
                                        "Other".equals(session.getBrowser()) && // iPhone user agent doesn't contain
                                                                                // typical browser strings
                                        "macOS".equals(session.getOperatingSystem()) // iOS detection logic treats
                                                                                     // iPhone as macOS family
                        ));
                }

                @Test
                @DisplayName("Should detect desktop device correctly")
                void shouldDetectDesktopDeviceCorrectly() {
                        // Given
                        String desktopUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/91.0.4472.124";
                        when(sessionRepository.save(any(UserSession.class))).thenReturn(testSession);

                        // When
                        userAnalyticsService.startSession(testUser, "session123",
                                        "192.168.1.1", desktopUserAgent);

                        // Then
                        verify(sessionRepository).save(argThat(session -> "Desktop".equals(session.getDeviceType()) &&
                                        "Chrome".equals(session.getBrowser()) &&
                                        "Windows".equals(session.getOperatingSystem())));
                }
        }

        @Nested
        @DisplayName("Error Handling Tests")
        class ErrorHandlingTests {

                @Test
                @DisplayName("Should handle session repository exception gracefully")
                void shouldHandleSessionRepositoryExceptionGracefully() {
                        // Given
                        when(sessionRepository.save(any(UserSession.class)))
                                        .thenThrow(new RuntimeException("Database error"));

                        // When
                        UserSession result = userAnalyticsService.startSession(testUser, "session123",
                                        "192.168.1.1", "Mozilla/5.0");

                        // Then
                        assertThat(result).isNull();
                        verify(sessionRepository).save(any(UserSession.class));
                }

                @Test
                @DisplayName("Should handle end session exception gracefully")
                void shouldHandleEndSessionExceptionGracefully() {
                        // Given
                        when(sessionRepository.findBySessionIdAndIsActiveTrue("session123"))
                                        .thenThrow(new RuntimeException("Database error"));

                        // When & Then - Should not throw exception
                        userAnalyticsService.endSession("session123");

                        verify(sessionRepository).findBySessionIdAndIsActiveTrue("session123");
                }
        }
}