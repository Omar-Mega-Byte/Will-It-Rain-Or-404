package com.weather_found.weather_app.modules.location.service;

import com.weather_found.weather_app.modules.location.dto.ml.LocationMLRequest;

import com.weather_found.weather_app.modules.location.dto.request.CreateLocationRequest;
import com.weather_found.weather_app.modules.location.dto.request.UpdateLocationRequest;
import com.weather_found.weather_app.modules.location.dto.response.LocationResponse;
import com.weather_found.weather_app.modules.location.dto.response.LocationSummaryResponse;
import com.weather_found.weather_app.modules.location.exception.LocationAlreadyExistsException;
import com.weather_found.weather_app.modules.location.exception.LocationNotFoundException;
import com.weather_found.weather_app.modules.location.mapper.LocationMapper;
import com.weather_found.weather_app.modules.location.model.Location;
import com.weather_found.weather_app.modules.location.repository.LocationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service for location management operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class LocationService {
    /**
     * Get essential ML data for a location by ID
     */
    public LocationMLRequest getLocationForMLById(Long id) {
        log.debug("Fetching ML essential data for location with ID: {}", id);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));

        LocationMLRequest mlDto = new LocationMLRequest();
        mlDto.setLatitude(location.getLatitude());
        mlDto.setLongitude(location.getLongitude());
        mlDto.setTimezone(location.getTimezone());
        mlDto.setElevation(location.getElevation());
        mlDto.setBeginDate(location.getBeginDate());
        mlDto.setEndDate(location.getEndDate());
        return mlDto;
    }

    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    /**
     * Get all locations
     */
    public List<LocationSummaryResponse> getAllLocations() {
        log.debug("Fetching all locations");
        List<Location> locations = locationRepository.findAllByOrderByName();
        return locationMapper.toSummaryResponseList(locations);
    }

    /**
     * Get location by ID
     */
    public LocationResponse getLocationById(Long id) {
        log.debug("Fetching location with ID: {}", id);
        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));
        return locationMapper.toResponse(location);
    }

    /**
     * Search locations by query
     */
    public List<LocationSummaryResponse> searchLocations(String query) {
        log.debug("Searching locations with query: {}", query);
        List<Location> locations = locationRepository.searchLocationsByQuery(query);
        return locationMapper.toSummaryResponseList(locations);
    }

    /**
     * Search locations by query with pagination
     */
    public Page<LocationSummaryResponse> searchLocations(String query, Pageable pageable) {
        log.debug("Searching locations with query: {} and pagination", query);
        Page<Location> locationPage = locationRepository.searchLocationsByQuery(query, pageable);
        return locationPage.map(locationMapper::toSummaryResponse);
    }

    /**
     * Get locations by country
     */
    public List<LocationSummaryResponse> getLocationsByCountry(String country) {
        log.debug("Fetching locations by country: {}", country);
        List<Location> locations = locationRepository.findByCountryIgnoreCaseOrderByName(country);
        return locationMapper.toSummaryResponseList(locations);
    }

    /**
     * Get locations by country and state
     */
    public List<LocationSummaryResponse> getLocationsByCountryAndState(String country, String state) {
        log.debug("Fetching locations by country: {} and state: {}", country, state);
        List<Location> locations = locationRepository.findByCountryIgnoreCaseAndStateIgnoreCaseOrderByName(country,
                state);
        return locationMapper.toSummaryResponseList(locations);
    }

    /**
     * Get locations by city
     */
    public List<LocationSummaryResponse> getLocationsByCity(String city) {
        log.debug("Fetching locations by city: {}", city);
        List<Location> locations = locationRepository.findByCityIgnoreCaseOrderByName(city);
        return locationMapper.toSummaryResponseList(locations);
    }

    /**
     * Find nearby locations
     */
    public List<LocationSummaryResponse> findNearbyLocations(BigDecimal latitude, BigDecimal longitude,
            BigDecimal radius) {
        log.debug("Finding locations near ({}, {}) within radius {}", latitude, longitude, radius);
        List<Location> locations = locationRepository.findNearbyLocations(latitude, longitude, radius);
        return locationMapper.toSummaryResponseList(locations);
    }

    /**
     * Create a new location
     */
    @Transactional
    public LocationResponse createLocation(CreateLocationRequest request) {
        log.debug("Creating new location: {}", request.getName());

        // Check if location already exists with same coordinates
        if (locationRepository.existsByLatitudeAndLongitude(request.getLatitude(), request.getLongitude())) {
            throw new LocationAlreadyExistsException(request.getName(),
                    request.getLatitude() + ", " + request.getLongitude());
        }

        Location location = locationMapper.toEntity(request);
        Location savedLocation = locationRepository.save(location);

        log.info("Successfully created location with ID: {}", savedLocation.getId());
        return locationMapper.toResponse(savedLocation);
    }

    /**
     * Update an existing location
     */
    @Transactional
    public LocationResponse updateLocation(Long id, UpdateLocationRequest request) {
        log.debug("Updating location with ID: {}", id);

        Location location = locationRepository.findById(id)
                .orElseThrow(() -> new LocationNotFoundException(id));

        // Check if coordinates are being updated and don't conflict with existing
        // location
        if (request.getLatitude() != null && request.getLongitude() != null) {
            boolean exists = locationRepository.existsByLatitudeAndLongitude(request.getLatitude(),
                    request.getLongitude());
            if (exists && (!location.getLatitude().equals(request.getLatitude()) ||
                    !location.getLongitude().equals(request.getLongitude()))) {
                throw new LocationAlreadyExistsException("Location with coordinates",
                        request.getLatitude() + ", " + request.getLongitude());
            }
        }

        locationMapper.updateEntity(location, request);
        Location updatedLocation = locationRepository.save(location);

        log.info("Successfully updated location with ID: {}", id);
        return locationMapper.toResponse(updatedLocation);
    }

    /**
     * Delete a location
     */
    @Transactional
    public void deleteLocation(Long id) {
        log.debug("Deleting location with ID: {}", id);

        if (!locationRepository.existsById(id)) {
            throw new LocationNotFoundException(id);
        }

        locationRepository.deleteById(id);
        log.info("Successfully deleted location with ID: {}", id);
    }

    /**
     * Get recent locations
     */
    public List<LocationSummaryResponse> getRecentLocations() {
        log.debug("Fetching recent locations");
        List<Location> locations = locationRepository.findTop10ByOrderByCreatedAtDesc();
        return locationMapper.toSummaryResponseList(locations);
    }

    /**
     * Count locations by country
     */
    public long countLocationsByCountry(String country) {
        log.debug("Counting locations by country: {}", country);
        return locationRepository.countByCountry(country);
    }

    /**
     * Check if location exists by ID
     */
    public boolean locationExists(Long id) {
        return locationRepository.existsById(id);
    }

    /**
     * Check if location exists by coordinates
     */
    public boolean locationExistsByCoordinates(BigDecimal latitude, BigDecimal longitude) {
        return locationRepository.existsByLatitudeAndLongitude(latitude, longitude);
    }
}