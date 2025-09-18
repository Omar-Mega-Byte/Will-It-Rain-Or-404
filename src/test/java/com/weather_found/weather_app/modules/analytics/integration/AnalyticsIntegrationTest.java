package com.weather_found.weather_app.modules.analytics.integration;

import com.weather_found.weather_app.modules.analytics.model.AnalyticsEvent;
import com.weather_found.weather_app.modules.analytics.repository.AnalyticsRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class AnalyticsIntegrationTest {

    @Autowired
    private AnalyticsRepository analyticsRepository;

    @BeforeEach
    void setUp() {
        analyticsRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        analyticsRepository.deleteAll();
    }

    @Test
    void testSaveAndFindAnalyticsEvent() {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setType("USER_LOGIN");
        event.setTimestamp(LocalDateTime.now());
        event.setDetails("User logged in successfully");

        analyticsRepository.save(event);

        List<AnalyticsEvent> events = analyticsRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getType()).isEqualTo("USER_LOGIN");
        assertThat(events.get(0).getDetails()).isEqualTo("User logged in successfully");
    }

    @Test
    void testDeleteAnalyticsEvent() {
        AnalyticsEvent event = new AnalyticsEvent();
        event.setType("USER_LOGOUT");
        event.setTimestamp(LocalDateTime.now());
        event.setDetails("User logged out");

        AnalyticsEvent saved = analyticsRepository.save(event);
        analyticsRepository.deleteById(saved.getId());

        List<AnalyticsEvent> events = analyticsRepository.findAll();
        assertThat(events).isEmpty();
    }

    @Test
    void testFindByType() {
        AnalyticsEvent event1 = new AnalyticsEvent();
        event1.setType("USER_LOGIN");
        event1.setTimestamp(LocalDateTime.now());
        event1.setDetails("User logged in");

        AnalyticsEvent event2 = new AnalyticsEvent();
        event2.setType("USER_LOGOUT");
        event2.setTimestamp(LocalDateTime.now());
        event2.setDetails("User logged out");

        analyticsRepository.save(event1);
        analyticsRepository.save(event2);

        List<AnalyticsEvent> loginEvents = analyticsRepository.findByType("USER_LOGIN");
        assertThat(loginEvents).hasSize(1);
        assertThat(loginEvents.get(0).getType()).isEqualTo("USER_LOGIN");
    }
}
