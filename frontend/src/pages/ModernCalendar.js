import React, { useState, useEffect } from 'react';
import eventService from '../services/eventService';
import './ModernCalendar.css';

const ModernCalendar = () => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [selectedDate, setSelectedDate] = useState(new Date());
  const [events, setEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEventModal, setShowEventModal] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [viewMode, setViewMode] = useState('month'); // 'month', 'week', 'day'

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

  const getEventsForDate = (date) => {
    return events.filter(event => {
      const eventDate = new Date(event.dateTime);
      return eventDate.toDateString() === date.toDateString();
    });
  };

  const getMonthDays = () => {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();
    
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const startDate = new Date(firstDay);
    startDate.setDate(startDate.getDate() - firstDay.getDay());
    
    const days = [];
    const currentDay = new Date(startDate);
    
    while (currentDay <= lastDay || days.length % 7 !== 0) {
      days.push(new Date(currentDay));
      currentDay.setDate(currentDay.getDate() + 1);
    }
    
    return days;
  };

  const getWeekDays = () => {
    const startOfWeek = new Date(selectedDate);
    startOfWeek.setDate(selectedDate.getDate() - selectedDate.getDay());
    
    const days = [];
    for (let i = 0; i < 7; i++) {
      const day = new Date(startOfWeek);
      day.setDate(startOfWeek.getDate() + i);
      days.push(day);
    }
    
    return days;
  };

  const navigateMonth = (direction) => {
    const newDate = new Date(currentDate);
    newDate.setMonth(currentDate.getMonth() + direction);
    setCurrentDate(newDate);
  };

  const navigateWeek = (direction) => {
    const newDate = new Date(selectedDate);
    newDate.setDate(selectedDate.getDate() + (direction * 7));
    setSelectedDate(newDate);
  };

  const navigateDay = (direction) => {
    const newDate = new Date(selectedDate);
    newDate.setDate(selectedDate.getDate() + direction);
    setSelectedDate(newDate);
  };

  const navigate = (direction) => {
    switch (viewMode) {
      case 'month':
        navigateMonth(direction);
        break;
      case 'week':
        navigateWeek(direction);
        break;
      case 'day':
        navigateDay(direction);
        break;
      default:
        break;
    }
  };

  const formatDateHeader = () => {
    switch (viewMode) {
      case 'month':
        return currentDate.toLocaleDateString('en-US', { 
          month: 'long', 
          year: 'numeric' 
        });
      case 'week':
        const weekStart = new Date(selectedDate);
        weekStart.setDate(selectedDate.getDate() - selectedDate.getDay());
        const weekEnd = new Date(weekStart);
        weekEnd.setDate(weekStart.getDate() + 6);
        return `${weekStart.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })} - ${weekEnd.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}`;
      case 'day':
        return selectedDate.toLocaleDateString('en-US', { 
          weekday: 'long',
          month: 'long', 
          day: 'numeric',
          year: 'numeric' 
        });
      default:
        return '';
    }
  };

  const getEventTypeColor = (type) => {
    switch (type) {
      case 'weather_alert': return '#ff6b6b';
      case 'maintenance': return '#4ecdc4';
      case 'social': return '#667eea';
      case 'sports': return '#f093fb';
      case 'business': return '#4facfe';
      case 'personal': return '#43e97b';
      default: return '#a8a8a8';
    }
  };

  const handleDateClick = (date) => {
    setSelectedDate(date);
    if (viewMode === 'month') {
      setViewMode('day');
    }
  };

  const handleEventClick = (event) => {
    setSelectedEvent(event);
    setShowEventModal(true);
  };

  const isToday = (date) => {
    const today = new Date();
    return date.toDateString() === today.toDateString();
  };

  const isSelected = (date) => {
    return date.toDateString() === selectedDate.toDateString();
  };

  const isCurrentMonth = (date) => {
    return date.getMonth() === currentDate.getMonth();
  };

  if (isLoading) {
    return (
      <div className="modern-calendar">
        <div className="calendar-loading">
          <div className="loading-spinner"></div>
          <p>Loading calendar...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="modern-calendar">
      {/* Header */}
      <div className="calendar-header">
        <div className="header-content">
          <div className="header-title">
            <h1>Calendar</h1>
            <p>Manage your events and schedule</p>
          </div>
          <button 
            className="create-event-btn"
            onClick={() => setShowCreateModal(true)}
          >
            <span>+</span>
            New Event
          </button>
        </div>
      </div>

      {/* Controls */}
      <div className="calendar-controls">
        <div className="navigation-controls">
          <button 
            className="nav-btn"
            onClick={() => navigate(-1)}
          >
            ‹
          </button>
          <h2 className="date-header">{formatDateHeader()}</h2>
          <button 
            className="nav-btn"
            onClick={() => navigate(1)}
          >
            ›
          </button>
        </div>

        <div className="view-controls">
          <button 
            className={`view-btn ${viewMode === 'month' ? 'active' : ''}`}
            onClick={() => setViewMode('month')}
          >
            Month
          </button>
          <button 
            className={`view-btn ${viewMode === 'week' ? 'active' : ''}`}
            onClick={() => setViewMode('week')}
          >
            Week
          </button>
          <button 
            className={`view-btn ${viewMode === 'day' ? 'active' : ''}`}
            onClick={() => setViewMode('day')}
          >
            Day
          </button>
        </div>

        <button 
          className="today-btn"
          onClick={() => {
            setCurrentDate(new Date());
            setSelectedDate(new Date());
          }}
        >
          Today
        </button>
      </div>

      {/* Calendar Content */}
      <div className="calendar-content">
        {error && (
          <div className="error-message">
            <span>⚠️</span>
            {error}
          </div>
        )}

        {/* Month View */}
        {viewMode === 'month' && (
          <div className="month-view">
            <div className="weekdays">
              {['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'].map(day => (
                <div key={day} className="weekday">{day}</div>
              ))}
            </div>
            <div className="month-grid">
              {getMonthDays().map((date, index) => {
                const dayEvents = getEventsForDate(date);
                return (
                  <div 
                    key={index}
                    className={`calendar-day ${!isCurrentMonth(date) ? 'other-month' : ''} ${isToday(date) ? 'today' : ''} ${isSelected(date) ? 'selected' : ''}`}
                    onClick={() => handleDateClick(date)}
                  >
                    <div className="day-number">{date.getDate()}</div>
                    <div className="day-events">
                      {dayEvents.slice(0, 3).map((event, eventIndex) => (
                        <div 
                          key={eventIndex}
                          className="event-dot"
                          style={{ backgroundColor: getEventTypeColor(event.type) }}
                          title={event.title}
                          onClick={(e) => {
                            e.stopPropagation();
                            handleEventClick(event);
                          }}
                        />
                      ))}
                      {dayEvents.length > 3 && (
                        <div className="more-events">+{dayEvents.length - 3}</div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Week View */}
        {viewMode === 'week' && (
          <div className="week-view">
            <div className="week-header">
              {getWeekDays().map((date, index) => (
                <div 
                  key={index}
                  className={`week-day-header ${isToday(date) ? 'today' : ''} ${isSelected(date) ? 'selected' : ''}`}
                  onClick={() => setSelectedDate(date)}
                >
                  <div className="week-day-name">
                    {date.toLocaleDateString('en-US', { weekday: 'short' })}
                  </div>
                  <div className="week-day-number">{date.getDate()}</div>
                </div>
              ))}
            </div>
            <div className="week-grid">
              {getWeekDays().map((date, index) => {
                const dayEvents = getEventsForDate(date);
                return (
                  <div key={index} className="week-day-column">
                    {dayEvents.map((event, eventIndex) => (
                      <div 
                        key={eventIndex}
                        className="week-event"
                        style={{ backgroundColor: getEventTypeColor(event.type) }}
                        onClick={() => handleEventClick(event)}
                      >
                        <div className="event-time">
                          {new Date(event.dateTime).toLocaleTimeString('en-US', { 
                            hour: 'numeric', 
                            minute: '2-digit',
                            hour12: true 
                          })}
                        </div>
                        <div className="event-title">{event.title}</div>
                      </div>
                    ))}
                  </div>
                );
              })}
            </div>
          </div>
        )}

        {/* Day View */}
        {viewMode === 'day' && (
          <div className="day-view">
            <div className="day-header">
              <h3>{selectedDate.toLocaleDateString('en-US', { 
                weekday: 'long',
                month: 'long', 
                day: 'numeric' 
              })}</h3>
            </div>
            <div className="day-events-list">
              {getEventsForDate(selectedDate).length === 0 ? (
                <div className="no-events-day">
                  <div className="no-events-icon">📅</div>
                  <p>No events for this day</p>
                  <button 
                    className="create-event-link"
                    onClick={() => setShowCreateModal(true)}
                  >
                    Create an event
                  </button>
                </div>
              ) : (
                getEventsForDate(selectedDate).map((event, index) => (
                  <div 
                    key={index}
                    className="day-event-card"
                    onClick={() => handleEventClick(event)}
                  >
                    <div 
                      className="event-color-bar"
                      style={{ backgroundColor: getEventTypeColor(event.type) }}
                    />
                    <div className="event-details">
                      <div className="event-time">
                        {new Date(event.dateTime).toLocaleTimeString('en-US', { 
                          hour: 'numeric', 
                          minute: '2-digit',
                          hour12: true 
                        })}
                      </div>
                      <div className="event-title">{event.title}</div>
                      <div className="event-description">{event.description}</div>
                      {event.location && (
                        <div className="event-location">
                          <span>📍</span>
                          {event.location}
                        </div>
                      )}
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>
        )}
      </div>

      {/* Modals */}
      {showCreateModal && (
        <div className="modal-overlay" onClick={() => setShowCreateModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Create New Event</h2>
            <p>Event creation form would go here</p>
            <button onClick={() => setShowCreateModal(false)}>Close</button>
          </div>
        </div>
      )}

      {showEventModal && selectedEvent && (
        <div className="modal-overlay" onClick={() => setShowEventModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2>{selectedEvent.title}</h2>
              <div 
                className="event-type-badge"
                style={{ backgroundColor: getEventTypeColor(selectedEvent.type) }}
              >
                {selectedEvent.type}
              </div>
            </div>
            <div className="modal-body">
              <p className="event-description">{selectedEvent.description}</p>
              <div className="event-meta">
                <div className="meta-item">
                  <strong>Date:</strong> {new Date(selectedEvent.dateTime).toLocaleDateString()}
                </div>
                <div className="meta-item">
                  <strong>Time:</strong> {new Date(selectedEvent.dateTime).toLocaleTimeString()}
                </div>
                {selectedEvent.location && (
                  <div className="meta-item">
                    <strong>Location:</strong> {selectedEvent.location}
                  </div>
                )}
              </div>
            </div>
            <div className="modal-actions">
              <button 
                className="btn btn-primary"
                onClick={() => setShowEventModal(false)}
              >
                Close
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ModernCalendar;