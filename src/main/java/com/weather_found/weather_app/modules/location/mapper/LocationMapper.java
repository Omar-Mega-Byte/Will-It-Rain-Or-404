package com.weather_found.weather_app.modules.location.mapper;

import com.weather_found.weather_app.modules.location.dto.request.CreateLocationRequest;
import com.weather_found.weather_app.modules.location.dto.request.UpdateLocationRequest;
import com.weather_found.weather_app.modules.location.dto.response.LocationResponse;
import com.weather_found.weather_app.modules.location.dto.response.LocationSummaryResponse;
import com.weather_found.weather_app.modules.location.model.Location;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for converting between Location entities and DTOs
 */
@Component
public class LocationMapper {

    /**
     * Convert CreateLocationRequest to Location entity
     */
    public Location toEntity(CreateLocationRequest request) {
        if (request == null) {
            return null;
        }

        Location location = new Location();
        location.setName(request.getName());
        location.setLatitude(request.getLatitude());
        location.setLongitude(request.getLongitude());
        location.setCountry(request.getCountry());
        location.setState(request.getState());
        location.setCity(request.getCity());
        location.setAddress(request.getAddress());
        location.setTimezone(request.getTimezone());
        location.setElevation(request.getElevation());
        location.setBeginDate(request.getBeginDate());
        location.setEndDate(request.getEndDate());

        return location;
    }

    /**
     * Convert Location entity to LocationResponse
     */
    public LocationResponse toResponse(Location location) {
        if (location == null) {
            return null;
        }

        return new LocationResponse(
                location.getId(),
                location.getName(),
                location.getLatitude(),
                location.getLongitude(),
                location.getCountry(),
                location.getState(),
                location.getCity(),
                location.getAddress(),
                location.getTimezone(),
                location.getElevation(),
                location.getBeginDate(),
                location.getEndDate(),
                location.getCreatedAt(),
                location.getUpdatedAt());
    }

    /**
     * Convert Location entity to LocationSummaryResponse
     */
    public LocationSummaryResponse toSummaryResponse(Location location) {
        if (location == null) {
            return null;
        }

        return new LocationSummaryResponse(
                location.getId(),
                location.getName(),
                location.getLatitude(),
                location.getLongitude(),
                location.getCountry(),
                location.getState(),
                location.getCity(),
                location.getTimezone(),
                location.getBeginDate(),
                location.getEndDate());
    }

    /**
     * Convert list of Location entities to list of LocationResponse
     */
    public List<LocationResponse> toResponseList(List<Location> locations) {
        if (locations == null) {
            return null;
        }

        return locations.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Convert list of Location entities to list of LocationSummaryResponse
     */
    public List<LocationSummaryResponse> toSummaryResponseList(List<Location> locations) {
        if (locations == null) {
            return null;
        }

        return locations.stream()
                .map(this::toSummaryResponse)
                .collect(Collectors.toList());
    }

    /**
     * Update existing Location entity with UpdateLocationRequest data
     */
    public void updateEntity(Location location, UpdateLocationRequest request) {
        if (location == null || request == null) {
            return;
        }

        if (request.getName() != null) {
            location.setName(request.getName());
        }
        if (request.getLatitude() != null) {
            location.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            location.setLongitude(request.getLongitude());
        }
        if (request.getCountry() != null) {
            location.setCountry(request.getCountry());
        }
        if (request.getState() != null) {
            location.setState(request.getState());
        }
        if (request.getCity() != null) {
            location.setCity(request.getCity());
        }
        if (request.getAddress() != null) {
            location.setAddress(request.getAddress());
        }
        if (request.getTimezone() != null) {
            location.setTimezone(request.getTimezone());
        }
        if (request.getElevation() != null) {
            location.setElevation(request.getElevation());
        }
        if (request.getBeginDate() != null) {
            location.setBeginDate(request.getBeginDate());
        }
        if (request.getEndDate() != null) {
            location.setEndDate(request.getEndDate());
        }
    }
}