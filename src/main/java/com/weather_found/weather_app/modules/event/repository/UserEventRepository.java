package com.weather_found.weather_app.modules.event.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.weather_found.weather_app.modules.event.model.Event;
import com.weather_found.weather_app.modules.user.model.User;

@Repository
public interface UserEventRepository extends JpaRepository<Event, Long> {
    @Modifying
    @Query(value = "DELETE FROM user_events WHERE event_id = :eventId", nativeQuery = true)
    void deleteByEventId(@Param("eventId") Long eventId);
}
