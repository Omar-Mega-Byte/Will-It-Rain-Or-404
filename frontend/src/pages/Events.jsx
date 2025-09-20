import React, { useState, useEffect } from 'react';
import EventList from '../components/EventList';
import EventForm from '../components/EventForm';
import EventModal from '../components/EventModal';
import EventSearch from '../components/EventSearch';
import LoadingSpinner from '../components/LoadingSpinner';
import eventService from '../services/eventService';
import './Events.css';

const Events = () => {
  // State management
  const [events, setEvents] = useState([]);
  const [filteredEvents, setFilteredEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Pagination state
  const [currentPage, setCurrentPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [pageSize] = useState(10);
  
  // Modal and form states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [showDeleteModal, setShowDeleteModal] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  
  // Search and filter state
  const [isSearching, setIsSearching] = useState(false);
  const [searchActive, setSearchActive] = useState(false);
  const [currentFilters, setCurrentFilters] = useState({});
  
  // View state
  const [viewMode, setViewMode] = useState('list'); // 'list' or 'compact'

  // Load initial events
  useEffect(() => {
    loadEvents();
  }, [currentPage, pageSize]);

  // Load events from API
  const loadEvents = async (useSearch = false, searchFilters = {}) => {
    try {
      setIsLoading(true);
      setError(null);

      let response;
      if (useSearch && Object.keys(searchFilters).length > 0) {
        setIsSearching(true);
        response = await eventService.searchEvents(searchFilters);
        setFilteredEvents(response.data || []);
        setSearchActive(true);
        setCurrentFilters(searchFilters);
      } else {
        setIsSearching(false);
        response = await eventService.getUserEvents(currentPage, pageSize);
        if (response.data) {
          setEvents(response.data.events || []);
          setTotalPages(response.data.totalPages || 0);
          setFilteredEvents(response.data.events || []);
        }
        setSearchActive(false);
        setCurrentFilters({});
      }
    } catch (err) {
      setError(err.message || 'Failed to load events');
      setEvents([]);
      setFilteredEvents([]);
    } finally {
      setIsLoading(false);
    }
  };

  // Event handlers
  const handleSearch = (searchFilters) => {
    setCurrentPage(0);
    loadEvents(true, searchFilters);
  };

  const handleClearSearch = () => {
    setCurrentPage(0);
    loadEvents(false);
  };

  const handleCreateEvent = async (eventData) => {
    try {
      setIsSubmitting(true);
      await eventService.createEvent(eventData);
      setShowCreateModal(false);
      
      if (searchActive) {
        loadEvents(true, currentFilters);
      } else {
        loadEvents();
      }
      
      console.log('Event created successfully');
    } catch (err) {
      throw err;
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
      
      if (searchActive) {
        loadEvents(true, currentFilters);
      } else {
        loadEvents();
      }
      
      console.log('Event updated successfully');
    } catch (err) {
      throw err;
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleDeleteEvent = async () => {
    try {
      setIsSubmitting(true);
      await eventService.deleteEvent(selectedEvent.id);
      setShowDeleteModal(false);
      setSelectedEvent(null);
      
      if (searchActive) {
        loadEvents(true, currentFilters);
      } else {
        loadEvents();
      }
      
      console.log('Event deleted successfully');
    } catch (err) {
      setError(err.message || 'Failed to delete event');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleViewEvent = (event) => {
    setSelectedEvent(event);
    setShowViewModal(true);
  };

  const handleEditEventClick = (event) => {
    setSelectedEvent(event);
    setShowEditModal(true);
  };

  const handleDeleteEventClick = (event) => {
    setSelectedEvent(event);
    setShowDeleteModal(true);
  };

  const handlePageChange = (page) => {
    setCurrentPage(page);
  };

  const getDisplayEvents = () => {
    return searchActive ? filteredEvents : events;
  };

  const getPageTitle = () => {
    const eventCount = getDisplayEvents().length;
    if (searchActive) {
      return `Search Results (${eventCount} found)`;
    }
    return `My Events (${eventCount})`;
  };

  return (
    <div className="events">
      <div className="container">
        {/* Header */}
        <div className="header">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h1 className="header-title">Events</h1>
              <p className="header-subtitle">Manage your events and stay organized</p>
            </div>
            
            <div className="header-actions">
              {/* View Mode Toggle */}
              <div className="view-toggle">
                <button
                  onClick={() => setViewMode('list')}
                  className={viewMode === 'list' ? 'active' : ''}
                >
                  <svg className="w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 10h16M4 14h16M4 18h16" />
                  </svg>
                </button>
                <button
                  onClick={() => setViewMode('compact')}
                  className={viewMode === 'compact' ? 'active' : ''}
                >
                  <svg className="w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                  </svg>
                </button>
              </div>

              {/* Create Event Button */}
              <button
                onClick={() => setShowCreateModal(true)}
                className="create-event-btn"
              >
                <svg className="w-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
                <span>Create Event</span>
              </button>
            </div>
          </div>
        </div>

        {/* Search Component */}
        <EventSearch
          onSearch={handleSearch}
          onClear={handleClearSearch}
          isLoading={isSearching}
          initialFilters={currentFilters}
        />

        {/* Events List */}
        <div className="event-list-container">
          <div className="event-list-header">
            <h2 className="event-list-title">{getPageTitle()}</h2>
            
            {searchActive && (
              <button
                onClick={handleClearSearch}
                className="clear-search-btn"
              >
                Clear Search
              </button>
            )}
          </div>

          <EventList
            events={getDisplayEvents()}
            isLoading={isLoading}
            error={error}
            onEdit={handleEditEventClick}
            onDelete={handleDeleteEventClick}
            onView={handleViewEvent}
            showActions={true}
            compact={viewMode === 'compact'}
            showPagination={!searchActive}
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={handlePageChange}
            emptyMessage={
              searchActive 
                ? 'No events match your search criteria. Try adjusting your filters.'
                : 'You haven\'t created any events yet. Click "Create Event" to get started.'
            }
          />
        </div>

        {/* Statistics Cards */}
        {!searchActive && getDisplayEvents().length > 0 && (
          <div className="stats-grid">
            <StatCard
              title="Total Events"
              value={getDisplayEvents().length}
              icon="📅"
              color="blue"
            />
            <StatCard
              title="Upcoming"
              value={getDisplayEvents().filter(e => new Date(e.startDate) > new Date()).length}
              icon="⏰"
              color="green"
            />
            <StatCard
              title="In Progress"
              value={getDisplayEvents().filter(e => e.eventStatus === 'IN_PROGRESS').length}
              icon="🔄"
              color="yellow"
            />
            <StatCard
              title="Completed"
              value={getDisplayEvents().filter(e => e.eventStatus === 'COMPLETED').length}
              icon="✅"
              color="purple"
            />
          </div>
        )}
      </div>

      {/* Modals */}
      
      {/* Create Event Modal */}
      {showCreateModal && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h2 className="modal-title">Create New Event</h2>
            </div>
            <div className="modal-content">
              <EventForm
                onSubmit={handleCreateEvent}
                onCancel={() => setShowCreateModal(false)}
                isLoading={isSubmitting}
              />
            </div>
          </div>
        </div>
      )}

      {/* Edit Event Modal */}
      {showEditModal && selectedEvent && (
        <div className="modal-overlay">
          <div className="modal">
            <div className="modal-header">
              <h2 className="modal-title">Edit Event</h2>
            </div>
            <div className="modal-content">
              <EventForm
                event={selectedEvent}
                onSubmit={handleEditEvent}
                onCancel={() => {
                  setShowEditModal(false);
                  setSelectedEvent(null);
                }}
                isLoading={isSubmitting}
              />
            </div>
          </div>
        </div>
      )}

      {/* View Event Modal */}
      <EventModal
        isOpen={showViewModal}
        onClose={() => {
          setShowViewModal(false);
          setSelectedEvent(null);
        }}
        event={selectedEvent}
        type="view"
      />

      {/* Delete Event Modal */}
      <EventModal
        isOpen={showDeleteModal}
        onClose={() => {
          setShowDeleteModal(false);
          setSelectedEvent(null);
        }}
        event={selectedEvent}
        type="delete"
        onConfirm={handleDeleteEvent}
        isLoading={isSubmitting}
      />
    </div>
  );
};

// Statistics Card Component
const StatCard = ({ title, value, icon, color }) => {
  return (
    <div className={`stat-card ${color}`}>
      <div className="stat-content">
        <div>
          <p className="stat-title">{title}</p>
          <p className="stat-value">{value}</p>
        </div>
        <div className="stat-icon">{icon}</div>
      </div>
    </div>
  );
};

export default Events;