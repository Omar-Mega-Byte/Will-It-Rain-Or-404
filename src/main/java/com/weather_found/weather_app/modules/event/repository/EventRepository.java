package com.weather_found.weather_app.modules.event.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.weather_found.weather_app.modules.event.model.Event;
import com.weather_found.weather_app.modules.event.model.enums.EventStatus;
import com.weather_found.weather_app.modules.event.model.enums.EventType;

/**
 * Repository for Event entity operations
 *
 * @author Weather Found Team
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    /**
     * Find events by user ID
     */
    @Query("SELECT e FROM Event e JOIN e.users u WHERE u.id = :userId")
    List<Event> findByUserId(@Param("userId") Long userId);

    /**
     * Find events by user ID with pagination
     */
    @Query("SELECT e FROM Event e JOIN e.users u WHERE u.id = :userId")
    Page<Event> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * Find events by name (case insensitive)
     */
    Optional<Event> findByEventNameIgnoreCase(String eventName);

    /**
     * Find events by status
     */
    List<Event> findByEventStatus(EventStatus eventStatus);

    /**
     * Find events by type
     */
    List<Event> findByEventType(EventType eventType);

    /**
     * Find events by date range
     */
    @Query("SELECT e FROM Event e WHERE e.startDate BETWEEN :startDate AND :endDate")
    List<Event> findByDateRange(@Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    /**
     * Find upcoming events for a user
     */
    @Query("SELECT e FROM Event e JOIN e.users u WHERE u.id = :userId AND e.startDate > :currentDate AND e.eventStatus = :status")
    List<Event> findUpcomingEventsByUserId(@Param("userId") Long userId,
            @Param("currentDate") LocalDateTime currentDate,
            @Param("status") EventStatus status);

    /**
     * Find conflicting events for a user in a time range
     */
    @Query("SELECT e FROM Event e JOIN e.users u WHERE u.id = :userId " +
            "AND e.eventStatus NOT IN (:excludedStatuses) " +
            "AND ((e.startDate BETWEEN :startDate AND :endDate) " +
            "OR (COALESCE(e.endDate, e.startDate) BETWEEN :startDate AND :endDate) " +
            "OR (e.startDate <= :startDate AND COALESCE(e.endDate, e.startDate) >= :endDate))")
    List<Event> findConflictingEvents(@Param("userId") Long userId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("excludedStatuses") List<EventStatus> excludedStatuses);

    /**
     * Check if event name exists for a user (excluding specific event)
     */
    @Query("SELECT COUNT(e) > 0 FROM Event e JOIN e.users u WHERE u.id = :userId " +
            "AND LOWER(e.eventName) = LOWER(:eventName) AND e.id != :excludeEventId")
    boolean existsByEventNameAndUserIdExcludingId(@Param("eventName") String eventName,
            @Param("userId") Long userId,
            @Param("excludeEventId") Long excludeEventId);

    /**
     * Check if event name exists for a user
     */
    @Query("SELECT COUNT(e) > 0 FROM Event e JOIN e.users u WHERE u.id = :userId " +
            "AND LOWER(e.eventName) = LOWER(:eventName)")
    boolean existsByEventNameAndUserId(@Param("eventName") String eventName,
            @Param("userId") Long userId);
}
