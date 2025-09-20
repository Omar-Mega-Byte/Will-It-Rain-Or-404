import React, { useState, useEffect } from 'react';
import EventCalendar from '../components/EventCalendar';
import EventForm from '../components/EventForm';
import LoadingSpinner from '../components/LoadingSpinner';
import eventService from '../services/eventService';
import './Calendar.css';

const Calendar = () => {
  const [events, setEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [selectedDate, setSelectedDate] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // Load events on component mount
  useEffect(() => {
    loadEvents();
  }, []);

  const loadEvents = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const response = await eventService.getAllUserEvents();
      setEvents(response.data || []);
    } catch (err) {
      setError(err.message || 'Failed to load events');
      setEvents([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDateClick = (date) => {
    setSelectedDate(date);
    setShowCreateModal(true);
  };

  const handleEventClick = (event) => {
    // Handle event click - you could show details modal or navigate to event page
    console.log('Event clicked:', event);
  };

  const handleCreateEvent = async (eventData) => {
    try {
      setIsSubmitting(true);
      
      // If a date was selected, use it as the start date
      if (selectedDate && !eventData.startDate) {
        const dateTime = new Date(selectedDate);
        dateTime.setHours(9, 0, 0, 0); // Default to 9 AM
        eventData.startDate = dateTime.toISOString();
      }
      
      await eventService.createEvent(eventData);
      setShowCreateModal(false);
      setSelectedDate(null);
      
      // Reload events
      await loadEvents();
      
      console.log('Event created successfully');
    } catch (err) {
      throw err; // Let the form handle the error
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleCloseCreateModal = () => {
    setShowCreateModal(false);
    setSelectedDate(null);
  };

  // Get default form data when creating from selected date
  const getDefaultFormData = () => {
    if (!selectedDate) return {};
    
    const dateTime = new Date(selectedDate);
    dateTime.setHours(9, 0, 0, 0); // Default to 9 AM
    
    const endDateTime = new Date(dateTime);
    endDateTime.setHours(10, 0, 0, 0); // Default to 1 hour duration
    
    return {
      startDate: dateTime.toISOString().slice(0, 16),
      endDate: endDateTime.toISOString().slice(0, 16)
    };
  };

  if (error && !isLoading) {
    return (
      <div className="calendar-error">
        <div className="calendar-error-icon">⚠️</div>
        <h3>Error Loading Calendar</h3>
        <p>{error}</p>
        <button className="calendar-error-btn" onClick={loadEvents}>
          Try Again
        </button>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="calendar-loading">
        <div className="calendar-loading-spinner"></div>
        <span>Loading calendar...</span>
      </div>
    );
  }

  return (
    <div className="calendar-page">
      <div className="calendar-container">
        {/* Header */}
        <div className="calendar-header">
          <div className="calendar-header-content">
            <div className="calendar-title-section">
              <h1>Calendar</h1>
              <p>View and manage your events in calendar format</p>
            </div>
            <div className="calendar-actions">
              <button className="create-event-btn" onClick={() => handleDateClick(new Date())}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M12 6v6m0 0v6m0-6h6m-6 0H6" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
                Create Event
              </button>
            </div>
          </div>
        </div>

        {/* Calendar Navigation */}
        <div className="calendar-nav">
          <button className="calendar-nav-btn">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M15 19l-7-7 7-7" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </button>
          <div className="calendar-current-month">September 2025</div>
          <button className="calendar-nav-btn">
            <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
              <path d="M9 5l7 7-7 7" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
            </svg>
          </button>
        </div>

        {/* Calendar Grid */}
        <div className="calendar-grid-container">
          <div className="calendar-grid">
            <div className="calendar-weekdays">
              <div className="calendar-weekday">Sun</div>
              <div className="calendar-weekday">Mon</div>
              <div className="calendar-weekday">Tue</div>
              <div className="calendar-weekday">Wed</div>
              <div className="calendar-weekday">Thu</div>
              <div className="calendar-weekday">Fri</div>
              <div className="calendar-weekday">Sat</div>
            </div>
            <EventCalendar
              events={events}
              onEventClick={handleEventClick}
              onDateClick={handleDateClick}
              isLoading={isLoading}
              showCreateButton={false}
            />
          </div>
        </div>

        {/* Quick Stats */}
        {events.length > 0 && (
          <div className="calendar-stats">
            <div className="calendar-stat-card blue">
              <div className="calendar-stat-content">
                <div className="calendar-stat-info">
                  <h3>This Month</h3>
                  <div className="value">{getEventsThisMonth()}</div>
                </div>
                <div className="calendar-stat-icon">📅</div>
              </div>
            </div>
            <div className="calendar-stat-card green">
              <div className="calendar-stat-content">
                <div className="calendar-stat-info">
                  <h3>This Week</h3>
                  <div className="value">{getEventsThisWeek()}</div>
                </div>
                <div className="calendar-stat-icon">📆</div>
              </div>
            </div>
            <div className="calendar-stat-card yellow">
              <div className="calendar-stat-content">
                <div className="calendar-stat-info">
                  <h3>Today</h3>
                  <div className="value">{getEventsToday()}</div>
                </div>
                <div className="calendar-stat-icon">⏰</div>
              </div>
            </div>
            <div className="calendar-stat-card purple">
              <div className="calendar-stat-content">
                <div className="calendar-stat-info">
                  <h3>Upcoming</h3>
                  <div className="value">{getUpcomingEvents()}</div>
                </div>
                <div className="calendar-stat-icon">🔜</div>
              </div>
            </div>
          </div>
        )}
      </div>

      {/* Create Event Modal */}
      {showCreateModal && (
        <div className="event-modal-overlay">
          <div className="event-modal-content">
            <div className="event-modal-header">
              <h2>
                Create New Event
                {selectedDate && (
                  <span className="event-modal-date-info">
                    for {selectedDate.toLocaleDateString()}
                  </span>
                )}
              </h2>
              <button className="event-modal-close-btn" onClick={handleCloseCreateModal}>
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor">
                  <path d="M18 6L6 18M6 6l12 12" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              </button>
            </div>
            <div className="event-modal-body">
              <EventForm
                event={getDefaultFormData()}
                onSubmit={handleCreateEvent}
                onCancel={handleCloseCreateModal}
                isLoading={isSubmitting}
              />
            </div>
          </div>
        </div>
      )}
    </div>
  );

  // Helper functions for stats
  function getEventsThisMonth() {
    const now = new Date();
    const thisMonth = now.getMonth();
    const thisYear = now.getFullYear();
    
    return events.filter(event => {
      const eventDate = new Date(event.startDate);
      return eventDate.getMonth() === thisMonth && eventDate.getFullYear() === thisYear;
    }).length;
  }

  function getEventsThisWeek() {
    const now = new Date();
    const startOfWeek = new Date(now);
    startOfWeek.setDate(now.getDate() - now.getDay());
    startOfWeek.setHours(0, 0, 0, 0);
    
    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 6);
    endOfWeek.setHours(23, 59, 59, 999);
    
    return events.filter(event => {
      const eventDate = new Date(event.startDate);
      return eventDate >= startOfWeek && eventDate <= endOfWeek;
    }).length;
  }

  function getEventsToday() {
    const today = new Date().toDateString();
    return events.filter(event => {
      const eventDate = new Date(event.startDate);
      return eventDate.toDateString() === today;
    }).length;
  }

  function getUpcomingEvents() {
    const now = new Date();
    return events.filter(event => {
      const eventDate = new Date(event.startDate);
      return eventDate > now && event.eventStatus === 'SCHEDULED';
    }).length;
  }
};

export default Calendar;