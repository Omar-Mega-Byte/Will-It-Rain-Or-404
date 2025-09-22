package com.weather_found.weather_app.modules.location;

import com.weather_found.weather_app.modules.location.controller.LocationController;
import com.weather_found.weather_app.modules.location.service.LocationService;
// ...existing code...
import com.weather_found.weather_app.modules.location.dto.response.LocationSummaryResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import java.util.Collections;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class LocationControllerTest {
    @Mock
    private LocationService locationService;

    @InjectMocks
    private LocationController locationController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetAllLocations() {
        LocationSummaryResponse summary = new LocationSummaryResponse();
        summary.setId(1L);
        summary.setBeginDate(java.time.LocalDate.of(2020, 1, 1));
        summary.setEndDate(java.time.LocalDate.of(2025, 12, 31));
        when(locationService.getAllLocations()).thenReturn(Collections.singletonList(summary));
        ResponseEntity<List<LocationSummaryResponse>> result = locationController.getAllLocations();
        assertNotNull(result.getBody());
        List<LocationSummaryResponse> body = result.getBody();
        assertNotNull(body);
        assertEquals(1, body.size());
        assertEquals(1L, body.get(0).getId());
    }
}