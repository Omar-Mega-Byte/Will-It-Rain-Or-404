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
        // Use search API
        setIsSearching(true);
        response = await eventService.searchEvents(searchFilters);
        setFilteredEvents(response.data || []);
        setSearchActive(true);
        setCurrentFilters(searchFilters);
      } else {
        // Use paginated API
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
      
      // Reload events
      if (searchActive) {
        loadEvents(true, currentFilters);
      } else {
        loadEvents();
      }
      
      // Show success message (you can implement toast notifications)
      console.log('Event created successfully');
    } catch (err) {
      throw err; // Let the form handle the error
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
      
      // Reload events
      if (searchActive) {
        loadEvents(true, currentFilters);
      } else {
        loadEvents();
      }
      
      console.log('Event updated successfully');
    } catch (err) {
      throw err; // Let the form handle the error
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
      
      // Reload events
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
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Events</h1>
              <p className="mt-2 text-gray-600">
                Manage your events and stay organized
              </p>
            </div>
            
            <div className="mt-4 sm:mt-0 flex items-center space-x-4">
              {/* View Mode Toggle */}
              <div className="flex items-center bg-white border border-gray-300 rounded-lg">
                <button
                  onClick={() => setViewMode('list')}
                  className={`px-3 py-2 text-sm font-medium rounded-l-lg ${
                    viewMode === 'list'
                      ? 'bg-blue-600 text-white'
                      : 'text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 10h16M4 14h16M4 18h16" />
                  </svg>
                </button>
                <button
                  onClick={() => setViewMode('compact')}
                  className={`px-3 py-2 text-sm font-medium rounded-r-lg ${
                    viewMode === 'compact'
                      ? 'bg-blue-600 text-white'
                      : 'text-gray-700 hover:bg-gray-50'
                  }`}
                >
                  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
                  </svg>
                </button>
              </div>

              {/* Create Event Button */}
              <button
                onClick={() => setShowCreateModal(true)}
                className="bg-blue-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 flex items-center space-x-2"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
        <div className="bg-white rounded-lg shadow-sm">
          <div className="p-6">
            <div className="flex items-center justify-between mb-6">
              <h2 className="text-xl font-semibold text-gray-900">
                {getPageTitle()}
              </h2>
              
              {searchActive && (
                <button
                  onClick={handleClearSearch}
                  className="text-sm text-blue-600 hover:text-blue-800 font-medium"
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
        </div>

        {/* Statistics Cards */}
        {!searchActive && getDisplayEvents().length > 0 && (
          <div className="mt-8 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
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
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-xl font-semibold text-gray-900">Create New Event</h2>
            </div>
            <div className="p-6">
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
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6 border-b border-gray-200">
              <h2 className="text-xl font-semibold text-gray-900">Edit Event</h2>
            </div>
            <div className="p-6">
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
  const getColorClasses = (color) => {
    const colors = {
      blue: 'bg-blue-50 text-blue-700 border-blue-200',
      green: 'bg-green-50 text-green-700 border-green-200',
      yellow: 'bg-yellow-50 text-yellow-700 border-yellow-200',
      purple: 'bg-purple-50 text-purple-700 border-purple-200',
      red: 'bg-red-50 text-red-700 border-red-200',
    };
    return colors[color] || colors.blue;
  };

  return (
    <div className={`p-4 rounded-lg border ${getColorClasses(color)}`}>
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium">{title}</p>
          <p className="text-2xl font-bold">{value}</p>
        </div>
        <div className="text-2xl">{icon}</div>
      </div>
    </div>
  );
};

export default Events;
