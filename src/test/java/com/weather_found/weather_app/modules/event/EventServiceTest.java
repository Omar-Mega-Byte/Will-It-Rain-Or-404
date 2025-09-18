package com.weather_found.weather_app.modules.event;

import com.weather_found.weather_app.modules.event.model.Event;
import com.weather_found.weather_app.modules.event.model.enums.EventType;
import com.weather_found.weather_app.modules.event.model.enums.EventStatus;
import com.weather_found.weather_app.modules.event.service.EventService;
import com.weather_found.weather_app.modules.event.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EventServiceTest {
    @Mock
    private EventRepository eventRepository;
    @Mock
    private com.weather_found.weather_app.modules.user.repository.UserRepository userRepository;
    @Mock
    private com.weather_found.weather_app.modules.event.mapper.EventMapper eventMapper;
    @Mock
    private com.weather_found.weather_app.modules.event.validation.EventValidation eventValidation;

    @InjectMocks
    private EventService eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testFindEventById() {
        Event event = new Event();
        event.setId(1L);
        event.setEventName("Test Event");
        com.weather_found.weather_app.modules.user.model.User user = new com.weather_found.weather_app.modules.user.model.User();
        user.setId(10L);
        user.setUsername("testuser");
        event.setUsers(Collections.singletonList(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(eventMapper.toEventResponseDto(event))
                .thenReturn(new com.weather_found.weather_app.modules.event.dto.response.EventResponseDto());
        com.weather_found.weather_app.modules.event.dto.response.EventResponseDto dto = eventService.getEventById(1L,
                "testuser");
        assertNotNull(dto);
    }

    @Test
    void testCreateEvent() {
        com.weather_found.weather_app.modules.event.dto.request.EventCreateDto createDto = new com.weather_found.weather_app.modules.event.dto.request.EventCreateDto();
        createDto.setEventName("New Event");
        com.weather_found.weather_app.modules.user.model.User user = new com.weather_found.weather_app.modules.user.model.User();
        user.setId(10L);
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(eventRepository.findByUserId(10L)).thenReturn(Collections.emptyList());
        Event event = new Event();
        event.setEventName("New Event");
        when(eventMapper.toEvent(createDto)).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.toEventResponseDto(event))
                .thenReturn(new com.weather_found.weather_app.modules.event.dto.response.EventResponseDto());
        com.weather_found.weather_app.modules.event.dto.response.EventResponseDto dto = eventService
                .createEvent(createDto, "testuser");
        assertNotNull(dto);
    }

    @Test
    void testFindAllEvents() {
        com.weather_found.weather_app.modules.user.model.User user = new com.weather_found.weather_app.modules.user.model.User();
        user.setId(10L);
        user.setUsername("testuser");
        Event event = new Event();
        event.setEventName("Event List");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(eventRepository.findByUserId(10L)).thenReturn(Collections.singletonList(event));
        when(eventMapper.toEventSummaryDtoList(anyList())).thenReturn(Collections.emptyList());
        assertNotNull(eventService.getUserEvents("testuser"));
    }

    @Test
    void testGetEventById_NotFound() {
        com.weather_found.weather_app.modules.user.model.User user = new com.weather_found.weather_app.modules.user.model.User();
        user.setId(10L);
        user.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(com.weather_found.weather_app.modules.event.exception.EventNotFoundException.class,
                () -> eventService.getEventById(99L, "testuser"));
    }

    @Test
    void testUpdateEvent() {
        com.weather_found.weather_app.modules.user.model.User user = new com.weather_found.weather_app.modules.user.model.User();
        user.setId(10L);
        user.setUsername("testuser");
        Event event = new Event();
        event.setId(2L);
        event.setEventName("Old Name");
        event.setUsers(Collections.singletonList(user));
        com.weather_found.weather_app.modules.event.dto.request.EventUpdateDto updateDto = new com.weather_found.weather_app.modules.event.dto.request.EventUpdateDto();
        updateDto.setEventName("Updated Name");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(eventRepository.findById(2L)).thenReturn(Optional.of(event));
        when(eventRepository.findByUserId(10L)).thenReturn(Collections.singletonList(event));
        when(eventMapper.updateEventFromDto(event, updateDto)).thenReturn(event);
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.toEventResponseDto(event))
                .thenReturn(new com.weather_found.weather_app.modules.event.dto.response.EventResponseDto());
        com.weather_found.weather_app.modules.event.dto.response.EventResponseDto dto = eventService.updateEvent(2L,
                updateDto, "testuser");
        assertNotNull(dto);
    }

    @Test
    void testDeleteEvent() {
        com.weather_found.weather_app.modules.user.model.User user = new com.weather_found.weather_app.modules.user.model.User();
        user.setId(10L);
        user.setUsername("testuser");
        Event event = new Event();
        event.setId(3L);
        event.setUsers(Collections.singletonList(user));
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(eventRepository.findById(3L)).thenReturn(Optional.of(event));
        doNothing().when(eventRepository).delete(any(Event.class));
        when(eventMapper.toEventResponseDto(event))
                .thenReturn(new com.weather_found.weather_app.modules.event.dto.response.EventResponseDto());
        doNothing().when(eventValidation).validateEventDeletion(any(Event.class));
        com.weather_found.weather_app.modules.event.dto.response.MessageResponseDto msg = eventService.deleteEvent(3L,
                "testuser");
        assertNotNull(msg);
    }

    // Remove testChangeEventStatus: No direct API for status change in EventService

    // Remove testFilterEventsByType: No direct API for filtering by type in
    // EventService
}
