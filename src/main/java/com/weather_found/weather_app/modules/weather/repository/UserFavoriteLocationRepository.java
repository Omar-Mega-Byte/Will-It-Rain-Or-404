package com.weather_found.weather_app.modules.weather.repository;

import com.weather_found.weather_app.modules.weather.model.Location;
import com.weather_found.weather_app.modules.weather.model.UserFavoriteLocation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for UserFavoriteLocation entities
 */
@Repository
public interface UserFavoriteLocationRepository extends JpaRepository<UserFavoriteLocation, Long> {

    /**
     * Find all favorite locations for a user
     */
    List<UserFavoriteLocation> findByUserIdOrderByCreatedAtAsc(Long userId);

    /**
     * Find specific favorite location for a user
     */
    Optional<UserFavoriteLocation> findByUserIdAndLocationId(Long userId, Long locationId);

    /**
     * Check if location is favorited by user
     */
    boolean existsByUserIdAndLocationId(Long userId, Long locationId);

    /**
     * Count favorite locations for a user
     */
    long countByUserId(Long userId);

    /**
     * Find users who favorited a specific location
     */
    List<UserFavoriteLocation> findByLocationOrderByCreatedAtAsc(Location location);

    /**
     * Find most popular locations (favorited by most users)
     */
    @Query("SELECT ufl.location, COUNT(ufl) as count FROM UserFavoriteLocation ufl " +
            "GROUP BY ufl.location ORDER BY count DESC")
    List<Object[]> findMostPopularLocations();

    /**
     * Find recently added favorite locations
     */
    List<UserFavoriteLocation> findTop10ByOrderByCreatedAtDesc();

    /**
     * Delete specific favorite location
     */
    void deleteByUserIdAndLocationId(Long userId, Long locationId);

    /**
     * Delete all favorites for a user
     */
    void deleteByUserId(Long userId);

    /**
     * Find favorite locations by user with custom names
     */
    @Query("SELECT ufl FROM UserFavoriteLocation ufl WHERE ufl.userId = :userId AND ufl.name IS NOT NULL")
    List<UserFavoriteLocation> findByUserIdWithCustomNames(@Param("userId") Long userId);
}
