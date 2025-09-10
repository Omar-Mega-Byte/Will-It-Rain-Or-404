package com.weather_found.weather_app.modules.weather.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * User favorite locations entity
 */
@Entity
@Table(name = "user_favorite_locations", uniqueConstraints = @UniqueConstraint(name = "uk_user_location", columnNames = {
        "user_id", "location_id" }), indexes = {
                @Index(name = "idx_user_favorites", columnList = "user_id"),
                @Index(name = "idx_location_favorites", columnList = "location_id")
        })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFavoriteLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false, foreignKey = @ForeignKey(name = "fk_favorite_location"))
    private Location location;

    @Column(length = 255)
    private String name; // Custom name for the location

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserFavoriteLocation(Long userId, Location location, String name) {
        this.userId = userId;
        this.location = location;
        this.name = name;
    }
}
