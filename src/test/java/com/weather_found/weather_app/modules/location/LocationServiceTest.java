package com.weather_found.weather_app.modules.location;

import com.weather_found.weather_app.modules.location.model.Location;
import com.weather_found.weather_app.modules.location.service.LocationService;
import com.weather_found.weather_app.modules.location.repository.LocationRepository;
import com.weather_found.weather_app.modules.location.mapper.LocationMapper;
import com.weather_found.weather_app.modules.location.dto.response.LocationSummaryResponse;
import com.weather_found.weather_app.modules.location.dto.request.CreateLocationRequest;
import com.weather_found.weather_app.modules.location.dto.request.UpdateLocationRequest;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import com.weather_found.weather_app.modules.location.dto.response.LocationResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class LocationServiceTest {
    @Test
    void testGetAllLocations() {
        Location location = new Location();
        location.setId(1L);
        location.setBeginDate(java.time.LocalDate.of(2020, 1, 1));
        location.setEndDate(java.time.LocalDate.of(2025, 12, 31));
        List<Location> locations = List.of(location);
        when(locationRepository.findAllByOrderByName()).thenReturn(locations);
        LocationSummaryResponse summary = new LocationSummaryResponse();
        summary.setId(1L);
        summary.setBeginDate(java.time.LocalDate.of(2020, 1, 1));
        summary.setEndDate(java.time.LocalDate.of(2025, 12, 31));
        when(locationMapper.toSummaryResponseList(locations)).thenReturn(List.of(summary));
        List<LocationSummaryResponse> result = locationService.getAllLocations();
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }

    @Test
    void testSearchLocations() {
        String query = "test";
        Location location = new Location();
        List<Location> locations = List.of(location);
        when(locationRepository.searchLocationsByQuery(query)).thenReturn(locations);
        LocationSummaryResponse summary = new LocationSummaryResponse();
        when(locationMapper.toSummaryResponseList(locations)).thenReturn(List.of(summary));
        List<LocationSummaryResponse> result = locationService.searchLocations(query);
        assertEquals(1, result.size());
    }

    @Test
    void testCreateLocation() {
        CreateLocationRequest request = new CreateLocationRequest();
        request.setName("Test");
        request.setLatitude(BigDecimal.ONE);
        request.setLongitude(BigDecimal.ONE);
        request.setBeginDate(java.time.LocalDate.of(2020, 1, 1));
        request.setEndDate(java.time.LocalDate.of(2025, 12, 31));
        when(locationRepository.existsByLatitudeAndLongitude(request.getLatitude(), request.getLongitude()))
                .thenReturn(false);
        Location location = new Location();
        when(locationMapper.toEntity(request)).thenReturn(location);
        Location savedLocation = new Location();
        savedLocation.setId(1L);
        when(locationRepository.save(location)).thenReturn(savedLocation);
        LocationResponse response = new LocationResponse();
        response.setId(1L);
        response.setBeginDate(java.time.LocalDate.of(2020, 1, 1));
        response.setEndDate(java.time.LocalDate.of(2025, 12, 31));
        when(locationMapper.toResponse(savedLocation)).thenReturn(response);
        LocationResponse result = locationService.createLocation(request);
        assertEquals(1L, result.getId());
    }

    @Test
    void testUpdateLocation() {
        Long id = 1L;
        UpdateLocationRequest request = new UpdateLocationRequest();
        Location location = new Location();
        location.setId(id);
        when(locationRepository.findById(id)).thenReturn(Optional.of(location));
        when(locationRepository.existsByLatitudeAndLongitude(any(), any())).thenReturn(false);
        doNothing().when(locationMapper).updateEntity(location, request);
        Location updatedLocation = new Location();
        updatedLocation.setId(id);
        when(locationRepository.save(location)).thenReturn(updatedLocation);
        LocationResponse response = new LocationResponse();
        response.setId(id);
        when(locationMapper.toResponse(updatedLocation)).thenReturn(response);
        LocationResponse result = locationService.updateLocation(id, request);
        assertEquals(id, result.getId());
    }

    @Test
    void testDeleteLocation() {
        Long id = 1L;
        when(locationRepository.existsById(id)).thenReturn(true);
        doNothing().when(locationRepository).deleteById(id);
        assertDoesNotThrow(() -> locationService.deleteLocation(id));
    }

    @Test
    void testGetLocationsByCountry() {
        String country = "Country";
        Location location = new Location();
        List<Location> locations = List.of(location);
        when(locationRepository.findByCountryIgnoreCaseOrderByName(country)).thenReturn(locations);
        LocationSummaryResponse summary = new LocationSummaryResponse();
        when(locationMapper.toSummaryResponseList(locations)).thenReturn(List.of(summary));
        List<LocationSummaryResponse> result = locationService.getLocationsByCountry(country);
        assertEquals(1, result.size());
    }

    @Test
    void testGetLocationsByCity() {
        String city = "City";
        Location location = new Location();
        List<Location> locations = List.of(location);
        when(locationRepository.findByCityIgnoreCaseOrderByName(city)).thenReturn(locations);
        LocationSummaryResponse summary = new LocationSummaryResponse();
        when(locationMapper.toSummaryResponseList(locations)).thenReturn(List.of(summary));
        List<LocationSummaryResponse> result = locationService.getLocationsByCity(city);
        assertEquals(1, result.size());
    }

    @Test
    void testFindNearbyLocations() {
        BigDecimal lat = BigDecimal.ONE;
        BigDecimal lon = BigDecimal.ONE;
        BigDecimal radius = BigDecimal.TEN;
        Location location = new Location();
        List<Location> locations = List.of(location);
        when(locationRepository.findNearbyLocations(lat, lon, radius)).thenReturn(locations);
        LocationSummaryResponse summary = new LocationSummaryResponse();
        when(locationMapper.toSummaryResponseList(locations)).thenReturn(List.of(summary));
        List<LocationSummaryResponse> result = locationService.findNearbyLocations(lat, lon, radius);
        assertEquals(1, result.size());
    }

    @Test
    void testGetRecentLocations() {
        Location location = new Location();
        List<Location> locations = List.of(location);
        when(locationRepository.findTop10ByOrderByCreatedAtDesc()).thenReturn(locations);
        LocationSummaryResponse summary = new LocationSummaryResponse();
        when(locationMapper.toSummaryResponseList(locations)).thenReturn(List.of(summary));
        List<LocationSummaryResponse> result = locationService.getRecentLocations();
        assertEquals(1, result.size());
    }

    @Test
    void testCountLocationsByCountry() {
        String country = "Country";
        when(locationRepository.countByCountry(country)).thenReturn(5L);
        long count = locationService.countLocationsByCountry(country);
        assertEquals(5L, count);
    }

    @Test
    void testLocationExists() {
        Long id = 1L;
        when(locationRepository.existsById(id)).thenReturn(true);
        assertTrue(locationService.locationExists(id));
    }

    @Test
    void testLocationExistsByCoordinates() {
        BigDecimal lat = BigDecimal.ONE;
        BigDecimal lon = BigDecimal.ONE;
        when(locationRepository.existsByLatitudeAndLongitude(lat, lon)).thenReturn(true);
        assertTrue(locationService.locationExistsByCoordinates(lat, lon));
    }

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationMapper locationMapper;

    @InjectMocks
    private LocationService locationService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetLocationById() {
        Location location = new Location();
        location.setId(1L);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        LocationResponse response = new LocationResponse();
        response.setId(1L);
        when(locationMapper.toResponse(location)).thenReturn(response);
        LocationResponse result = locationService.getLocationById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetLocationById_NotFound() {
        when(locationRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(Exception.class, () -> locationService.getLocationById(2L));
    }
}
