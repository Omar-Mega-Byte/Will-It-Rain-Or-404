package com.weather_found.weather_app.modules.analytics.repository;

import com.weather_found.weather_app.modules.analytics.model.AnalyticsEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AnalyticsRepository extends JpaRepository<AnalyticsEvent, Long> {
    List<AnalyticsEvent> findByType(String type);
}
