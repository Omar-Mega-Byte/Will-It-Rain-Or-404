package com.weather_found.weather_app.modules.weather.repository;

import com.weather_found.weather_app.modules.weather.model.Location;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Location entities in weather module
 */
@Repository
public interface WeatherLocationRepository extends JpaRepository<Location, Long> {

        /**
         * Find location by exact name
         */
        Optional<Location> findByNameIgnoreCase(String name);

        /**
         * Search locations by name containing given string
         */
        List<Location> findByNameContainingIgnoreCaseOrderByName(String name);

        /**
         * Find locations by country
         */
        List<Location> findByCountryIgnoreCaseOrderByName(String country);

        /**
         * Find locations by country and state
         */
        List<Location> findByCountryIgnoreCaseAndStateIgnoreCaseOrderByName(String country, String state);

        /**
         * Find locations within a bounding box (for map queries)
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
         * Find nearby locations within a radius (simplified calculation)
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
         * Find locations by timezone
         */
        List<Location> findByTimezoneOrderByName(String timezone);

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
}
