import React, { useState, useEffect } from 'react';
import eventService from '../services/eventService';
import './ModernEvents.css';

const ModernEvents = () => {
  // State management
  const [events, setEvents] = useState([]);
  const [filteredEvents, setFilteredEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize] = useState(12);
  
  // Modal and form states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  // Search and filter state
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState('all');
  const [filterStatus, setFilterStatus] = useState('all');
  const [sortBy, setSortBy] = useState('date');
  const [viewMode, setViewMode] = useState('grid'); // 'grid' or 'list'

  // Load initial events
  useEffect(() => {
    loadEvents();
  }, [currentPage, pageSize]);

  // Filter events when search/filter changes
  useEffect(() => {
    filterEvents();
  }, [events, searchQuery, filterType, filterStatus, sortBy]);

  // Load events from API
  const loadEvents = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      const response = await eventService.getUserEvents(currentPage, pageSize);
      if (response.data) {
        setEvents(response.data.events || []);
        setTotalPages(response.data.totalPages || 0);
      }
    } catch (err) {
      setError(err.message || 'Failed to load events');
      setEvents([]);
    } finally {
      setIsLoading(false);
    }
  };

  // Filter and search events
  const filterEvents = () => {
    let filtered = [...events];

    // Search filter
    if (searchQuery) {
      filtered = filtered.filter(event =>
        event.title?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        event.description?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        event.location?.toLowerCase().includes(searchQuery.toLowerCase())
      );
    }

    // Type filter
    if (filterType !== 'all') {
      filtered = filtered.filter(event => event.type === filterType);
    }

    // Status filter
    if (filterStatus !== 'all') {
      const now = new Date();
      filtered = filtered.filter(event => {
        const eventDate = new Date(event.dateTime);
        switch (filterStatus) {
          case 'upcoming':
            return eventDate > now;
          case 'past':
            return eventDate < now;
          case 'today':
            return eventDate.toDateString() === now.toDateString();
          default:
            return true;
        }
      });
    }

    // Sort events
    filtered.sort((a, b) => {
      switch (sortBy) {
        case 'date':
          return new Date(a.dateTime) - new Date(b.dateTime);
        case 'title':
          return a.title.localeCompare(b.title);
        case 'type':
          return a.type.localeCompare(b.type);
        default:
          return 0;
      }
    });

    setFilteredEvents(filtered);
  };

  const handleCreateEvent = async (eventData) => {
    try {
      setIsSubmitting(true);
      await eventService.createEvent(eventData);
      setShowCreateModal(false);
      loadEvents(); // Reload events
    } catch (err) {
      setError(err.message || 'Failed to create event');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleEditEvent = async (eventData) => {
    try {
      setIsSubmitting(true);
      await eventService.updateEvent(selectedEvent.id, eventData);
      setShowEditModal(false);
      setSelectedEvent(null);
      loadEvents(); // Reload events
    } catch (err) {
      setError(err.message || 'Failed to update event');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteEvent = async (eventId) => {
    try {
      await eventService.deleteEvent(eventId);
      loadEvents(); // Reload events
    } catch (err) {
      setError(err.message || 'Failed to delete event');
    }
  };

  const getEventIcon = (type) => {
    switch (type) {
      case 'weather_alert': return '⚡';
      case 'maintenance': return '🔧';
      case 'social': return '👥';
      case 'sports': return '⚽';
      case 'business': return '💼';
      case 'personal': return '👤';
      default: return '📅';
    }
  };

  const getEventTypeColor = (type) => {
    switch (type) {
      case 'weather_alert': return 'linear-gradient(135deg, #ff6b6b, #ff8e8e)';
      case 'maintenance': return 'linear-gradient(135deg, #4ecdc4, #44a08d)';
      case 'social': return 'linear-gradient(135deg, #667eea, #764ba2)';
      case 'sports': return 'linear-gradient(135deg, #f093fb, #f5576c)';
      case 'business': return 'linear-gradient(135deg, #4facfe, #00f2fe)';
      case 'personal': return 'linear-gradient(135deg, #43e97b, #38f9d7)';
      default: return 'linear-gradient(135deg, #a8edea, #fed6e3)';
    }
  };

  const formatEventDate = (dateTime) => {
    const date = new Date(dateTime);
    const now = new Date();
    const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
    const eventDate = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    const diffTime = eventDate - today;
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24));

    if (diffDays === 0) return 'Today';
    if (diffDays === 1) return 'Tomorrow';
    if (diffDays === -1) return 'Yesterday';
    if (diffDays > 0 && diffDays <= 7) return `In ${diffDays} days`;
    if (diffDays < 0 && diffDays >= -7) return `${Math.abs(diffDays)} days ago`;

    return date.toLocaleDateString('en-US', {
      month: 'short',
      day: 'numeric',
      year: date.getFullYear() !== now.getFullYear() ? 'numeric' : undefined
    });
  };

  const formatEventTime = (dateTime) => {
    return new Date(dateTime).toLocaleTimeString('en-US', {
      hour: 'numeric',
      minute: '2-digit',
      hour12: true
    });
  };

  if (isLoading) {
    return (
      <div className="modern-events">
        <div className="events-loading">
          <div className="loading-spinner"></div>
          <p>Loading events...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="modern-events">
      {/* Header Section */}
      <div className="events-header">
        <div className="header-content">
          <div className="header-title">
            <h1>Events</h1>
            <p>Manage your weather events and activities</p>
          </div>
          <button 
            className="create-event-btn"
            onClick={() => setShowCreateModal(true)}
          >
            <span>+</span>
            Create Event
          </button>
        </div>
      </div>

      {/* Controls Section */}
      <div className="events-controls">
        <div className="search-section">
          <div className="search-box">
            <span className="search-icon">🔍</span>
            <input
              type="text"
              placeholder="Search events..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
            />
          </div>
        </div>

        <div className="filter-section">
          <select 
            value={filterType} 
            onChange={(e) => setFilterType(e.target.value)}
            className="filter-select"
          >
            <option value="all">All Types</option>
            <option value="weather_alert">Weather Alerts</option>
            <option value="maintenance">Maintenance</option>
            <option value="social">Social</option>
            <option value="sports">Sports</option>
            <option value="business">Business</option>
            <option value="personal">Personal</option>
          </select>

          <select 
            value={filterStatus} 
            onChange={(e) => setFilterStatus(e.target.value)}
            className="filter-select"
          >
            <option value="all">All Events</option>
            <option value="upcoming">Upcoming</option>
            <option value="today">Today</option>
            <option value="past">Past</option>
          </select>

          <select 
            value={sortBy} 
            onChange={(e) => setSortBy(e.target.value)}
            className="filter-select"
          >
            <option value="date">Sort by Date</option>
            <option value="title">Sort by Title</option>
            <option value="type">Sort by Type</option>
          </select>
        </div>

        <div className="view-controls">
          <button 
            className={`view-btn ${viewMode === 'grid' ? 'active' : ''}`}
            onClick={() => setViewMode('grid')}
          >
            ⊞
          </button>
          <button 
            className={`view-btn ${viewMode === 'list' ? 'active' : ''}`}
            onClick={() => setViewMode('list')}
          >
            ☰
          </button>
        </div>
      </div>

      {/* Events Section */}
      <div className="events-content">
        {error && (
          <div className="error-message">
            <span>⚠️</span>
            {error}
          </div>
        )}

        {filteredEvents.length === 0 ? (
          <div className="no-events">
            <div className="no-events-icon">📅</div>
            <h3>No events found</h3>
            <p>Create your first event or adjust your search filters</p>
            <button 
              className="create-first-btn"
              onClick={() => setShowCreateModal(true)}
            >
              Create Event
            </button>
          </div>
        ) : (
          <div className={`events-grid ${viewMode}`}>
            {filteredEvents.map((event) => (
              <div key={event.id} className="event-card">
                <div className="event-header">
                  <div 
                    className="event-icon"
                    style={{ background: getEventTypeColor(event.type) }}
                  >
                    {getEventIcon(event.type)}
                  </div>
                  <div className="event-type">{event.type}</div>
                </div>

                <div className="event-content">
                  <h3 className="event-title">{event.title}</h3>
                  <p className="event-description">{event.description}</p>
                  
                  <div className="event-details">
                    <div className="event-date">
                      <span className="date-text">{formatEventDate(event.dateTime)}</span>
                      <span className="time-text">{formatEventTime(event.dateTime)}</span>
                    </div>
                    
                    {event.location && (
                      <div className="event-location">
                        <span>📍</span>
                        {event.location}
                      </div>
                    )}
                  </div>
                </div>

                <div className="event-actions">
                  <button 
                    className="action-btn view-btn"
                    onClick={() => {
                      setSelectedEvent(event);
                      setShowViewModal(true);
                    }}
                  >
                    View
                  </button>
                  <button 
                    className="action-btn edit-btn"
                    onClick={() => {
                      setSelectedEvent(event);
                      setShowEditModal(true);
                    }}
                  >
                    Edit
                  </button>
                  <button 
                    className="action-btn delete-btn"
                    onClick={() => handleDeleteEvent(event.id)}
                  >
                    Delete
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="pagination">
            <button 
              className="page-btn"
              disabled={currentPage === 0}
              onClick={() => setCurrentPage(currentPage - 1)}
            >
              Previous
            </button>
            
            <div className="page-info">
              Page {currentPage + 1} of {totalPages}
            </div>
            
            <button 
              className="page-btn"
              disabled={currentPage >= totalPages - 1}
              onClick={() => setCurrentPage(currentPage + 1)}
            >
              Next
            </button>
          </div>
        )}
      </div>

      {/* Modals would go here - simplified for now */}
      {showCreateModal && (
        <div className="modal-overlay" onClick={() => setShowCreateModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>Create New Event</h2>
            <p>Event creation form would go here</p>
            <button onClick={() => setShowCreateModal(false)}>Close</button>
          </div>
        </div>
      )}

      {showViewModal && selectedEvent && (
        <div className="modal-overlay" onClick={() => setShowViewModal(false)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <h2>{selectedEvent.title}</h2>
            <p>{selectedEvent.description}</p>
            <button onClick={() => setShowViewModal(false)}>Close</button>
          </div>
        </div>
      )}
    </div>
  );
};

export default ModernEvents;