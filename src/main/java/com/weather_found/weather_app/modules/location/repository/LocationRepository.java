package com.weather_found.weather_app.modules.location.repository;

import com.weather_found.weather_app.modules.location.model.Location;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Location entity operations
 */
@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {

        /**
         * Find location by exact name (case insensitive)
         */
        Optional<Location> findByNameIgnoreCase(String name);

        /**
         * Search locations by name containing given string (case insensitive)
         */
        @Query("SELECT l FROM Location l WHERE LOWER(l.name) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY l.name")
        List<Location> findByNameContainingIgnoreCaseOrderByName(@Param("name") String name);

        /**
         * Find locations by country (case insensitive)
         */
        List<Location> findByCountryIgnoreCaseOrderByName(String country);

        /**
         * Find locations by country and state (case insensitive)
         */
        List<Location> findByCountryIgnoreCaseAndStateIgnoreCaseOrderByName(String country, String state);

        /**
         * Find locations by city (case insensitive)
         */
        List<Location> findByCityIgnoreCaseOrderByName(String city);

        /**
         * Search locations by multiple criteria
         */
        @Query("SELECT l FROM Location l WHERE " +
                        "LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(l.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(l.country) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(l.state) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "ORDER BY l.name")
        List<Location> searchLocationsByQuery(@Param("query") String query);

        /**
         * Search locations by multiple criteria with pagination
         */
        @Query("SELECT l FROM Location l WHERE " +
                        "LOWER(l.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(l.city) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(l.country) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                        "LOWER(l.state) LIKE LOWER(CONCAT('%', :query, '%'))")
        Page<Location> searchLocationsByQuery(@Param("query") String query, Pageable pageable);

        /**
         * Find locations within a bounding box
         */
        @Query("SELECT l FROM Location l WHERE " +
                        "l.latitude BETWEEN :minLat AND :maxLat AND " +
                        "l.longitude BETWEEN :minLng AND :maxLng " +
                        "ORDER BY l.name")
        List<Location> findLocationsInBounds(
                        @Param("minLat") BigDecimal minLatitude,
                        @Param("maxLat") BigDecimal maxLatitude,
                        @Param("minLng") BigDecimal minLongitude,
                        @Param("maxLng") BigDecimal maxLongitude);

        /**
         * Find nearby locations (simplified calculation)
         */
        @Query("SELECT l FROM Location l WHERE " +
                        "ABS(l.latitude - :lat) <= :radius AND " +
                        "ABS(l.longitude - :lng) <= :radius " +
                        "ORDER BY l.name")
        List<Location> findNearbyLocations(
                        @Param("lat") BigDecimal latitude,
                        @Param("lng") BigDecimal longitude,
                        @Param("radius") BigDecimal radius);

        /**
         * Check if location exists with same coordinates
         */
        boolean existsByLatitudeAndLongitude(BigDecimal latitude, BigDecimal longitude);

        /**
         * Count locations by country
         */
        @Query("SELECT COUNT(l) FROM Location l WHERE LOWER(l.country) = LOWER(:country)")
        long countByCountry(@Param("country") String country);

        /**
         * Find most recently added locations
         */
        List<Location> findTop10ByOrderByCreatedAtDesc();

        /**
         * Find all locations ordered by name
         */
        List<Location> findAllByOrderByName();
}