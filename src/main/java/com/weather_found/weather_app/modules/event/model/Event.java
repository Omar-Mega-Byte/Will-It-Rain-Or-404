package com.weather_found.weather_app.modules.event.model;

import java.time.LocalDateTime;
import java.util.List;

import com.weather_found.weather_app.modules.shared.Base.BaseEntity;
import com.weather_found.weather_app.modules.user.model.User;
import com.weather_found.weather_app.modules.event.model.enums.EventType;
import com.weather_found.weather_app.modules.event.model.enums.EventStatus;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Entity
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
@Table(name = "events", indexes = {
        @Index(columnList = "event_name")
})
public class Event extends BaseEntity {
    @ManyToMany(mappedBy = "events", cascade = { CascadeType.PERSIST, CascadeType.MERGE })
    private List<User> users;

    // private Location location;

    @Column(name = "event_name", nullable = false, unique = true)
    private String eventName;

    @Column(name = "event_description", nullable = false)
    private String eventDescription;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    private LocalDateTime endDate;

    @Column(name = "is_outdoor", nullable = false)
    private Boolean isOutdoor;

    @Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "event_status", nullable = false)
    private EventStatus eventStatus;
}
