import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import eventService from '../services/eventService';
import EventCard from '../components/EventCard';
import EventForm from '../components/EventForm';
import EventModal from '../components/EventModal';
import EventSearch from '../components/EventSearch';
import Pagination from '../components/Pagination';

const Events = () => {
  const [user, setUser] = useState(null);
  const [events, setEvents] = useState([]);
  const [filteredEvents, setFilteredEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  
  // Modal states
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showEditModal, setShowEditModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);
  
  // Pagination
  const [currentPage, setCurrentPage] = useState(1);
  const [eventsPerPage] = useState(6);
  
  // Search and filters
  const [searchFilters, setSearchFilters] = useState({});
  
  const navigate = useNavigate();

  useEffect(() => {
    // Check authentication
    const token = localStorage.getItem('token');
    if (!token) {
      navigate('/login');
      return;
    }

    const userData = { username: 'User', email: 'user@example.com' };
    setUser(userData);
    loadEvents();
  }, [navigate]);

  const loadEvents = async () => {
    try {
      setIsLoading(true);
      const response = await eventService.getAllUserEvents();
      setEvents(response.data || []);
      setFilteredEvents(response.data || []);
    } catch (err) {
      setError('Failed to load events');
      setEvents([]);
      setFilteredEvents([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreateEvent = async (eventData) => {
    try {
      await eventService.createEvent(eventData);
      await loadEvents();
      setShowCreateModal(false);
    } catch (err) {
      throw new Error('Failed to create event');
    }
  };

  const handleEditEvent = async (eventData) => {
    try {
      await eventService.updateEvent(selectedEvent.eventId, eventData);
      await loadEvents();
      setShowEditModal(false);
      setSelectedEvent(null);
    } catch (err) {
      throw new Error('Failed to update event');
    }
  };

  const handleDeleteEvent = async (eventId) => {
    try {
      await eventService.deleteEvent(eventId);
      await loadEvents();
      setShowViewModal(false);
      setSelectedEvent(null);
    } catch (err) {
      throw new Error('Failed to delete event');
    }
  };

  const handleSearch = (filters) => {
    setSearchFilters(filters);
    let filtered = [...events];

    // Apply search filters
    if (filters.searchTerm) {
      filtered = filtered.filter(event =>
        event.eventName.toLowerCase().includes(filters.searchTerm.toLowerCase()) ||
        event.description?.toLowerCase().includes(filters.searchTerm.toLowerCase())
      );
    }

    if (filters.eventType && filters.eventType !== 'ALL') {
      filtered = filtered.filter(event => event.eventType === filters.eventType);
    }

    if (filters.eventStatus && filters.eventStatus !== 'ALL') {
      filtered = filtered.filter(event => event.eventStatus === filters.eventStatus);
    }

    if (filters.isOutdoor !== null) {
      filtered = filtered.filter(event => event.isOutdoor === filters.isOutdoor);
    }

    setFilteredEvents(filtered);
    setCurrentPage(1);
  };

  const handleLogout = () => {
    authService.logout();
    navigate('/');
  };

  // Pagination logic
  const indexOfLastEvent = currentPage * eventsPerPage;
  const indexOfFirstEvent = indexOfLastEvent - eventsPerPage;
  const currentEvents = filteredEvents.slice(indexOfFirstEvent, indexOfLastEvent);
  const totalPages = Math.ceil(filteredEvents.length / eventsPerPage);

  if (!user) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center">
        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Header */}
      <header className="bg-white shadow-sm border-b border-gray-200">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between items-center h-16">
            {/* Logo */}
            <div className="flex items-center">
              <span className="text-2xl mr-3">🌦️</span>
              <h1 className="text-xl font-bold text-gray-900">WeatherVision Events</h1>
            </div>
            
            {/* Navigation */}
            <nav className="hidden md:flex items-center space-x-8">
              <Link
                to="/dashboard"
                className="text-gray-700 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
              >
                Dashboard
              </Link>
              <Link
                to="/events"
                className="text-blue-600 hover:text-blue-700 px-3 py-2 rounded-md text-sm font-medium"
              >
                Events
              </Link>
              <Link
                to="/calendar"
                className="text-gray-700 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
              >
                Calendar
              </Link>
            </nav>

            {/* User menu */}
            <div className="flex items-center space-x-4">
              <span className="text-gray-700 text-sm">Welcome, {user.username}!</span>
              <button
                onClick={handleLogout}
                className="bg-red-600 hover:bg-red-700 text-white px-4 py-2 rounded-md text-sm font-medium"
              >
                Logout
              </button>
            </div>
          </div>
        </div>
      </header>

      {/* Main Content */}
      <main className="max-w-7xl mx-auto py-6 sm:px-6 lg:px-8">
        <div className="px-4 py-6 sm:px-0">
          
          {/* Page Header */}
          <div className="mb-8">
            <div className="sm:flex sm:items-center sm:justify-between">
              <div>
                <h1 className="text-2xl font-bold text-gray-900">Events</h1>
                <p className="mt-2 text-sm text-gray-700">
                  Manage your events and stay organized
                </p>
              </div>
              <div className="mt-4 sm:mt-0">
                <button
                  onClick={() => setShowCreateModal(true)}
                  className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500"
                >
                  <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                  </svg>
                  Create Event
                </button>
              </div>
            </div>
          </div>

          {/* Search and Filters */}
          <div className="mb-6">
            <EventSearch onSearch={handleSearch} />
          </div>

          {/* Events Grid */}
          {isLoading ? (
            <div className="text-center py-12">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
              <p className="mt-4 text-gray-500">Loading events...</p>
            </div>
          ) : error ? (
            <div className="text-center py-12">
              <svg className="w-12 h-12 text-red-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <p className="text-red-600">{error}</p>
              <button
                onClick={loadEvents}
                className="mt-4 text-blue-600 hover:text-blue-500"
              >
                Try again
              </button>
            </div>
          ) : filteredEvents.length === 0 ? (
            <div className="text-center py-12">
              <svg className="w-12 h-12 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <h3 className="text-lg font-medium text-gray-900 mb-2">No events found</h3>
              <p className="text-gray-500 mb-4">Get started by creating your first event.</p>
              <button
                onClick={() => setShowCreateModal(true)}
                className="inline-flex items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
              >
                <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
                Create Your First Event
              </button>
            </div>
          ) : (
            <>
              {/* Events Header Info */}
              <div className="mb-6 flex items-center justify-between">
                <h2 className="text-lg font-medium text-gray-900">
                  My Events ({filteredEvents.length})
                </h2>
                <p className="text-sm text-gray-500">
                  Showing {indexOfFirstEvent + 1} to {Math.min(indexOfLastEvent, filteredEvents.length)} of {filteredEvents.length} events
                </p>
              </div>

              {/* Events Grid */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-6">
                {currentEvents.map((event) => (
                  <EventCard
                    key={event.eventId}
                    event={event}
                    onView={(event) => {
                      setSelectedEvent(event);
                      setShowViewModal(true);
                    }}
                    onEdit={(event) => {
                      setSelectedEvent(event);
                      setShowEditModal(true);
                    }}
                    onDelete={(eventId) => handleDeleteEvent(eventId)}
                    compact={false}
                  />
                ))}
              </div>

              {/* Pagination */}
              {totalPages > 1 && (
                <Pagination
                  currentPage={currentPage}
                  totalPages={totalPages}
                  onPageChange={setCurrentPage}
                />
              )}
            </>
          )}

        </div>
      </main>

      {/* Modals */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-900">Create New Event</h3>
              <button
                onClick={() => setShowCreateModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <EventForm
              onSubmit={handleCreateEvent}
              onCancel={() => setShowCreateModal(false)}
            />
          </div>
        </div>
      )}

      {showEditModal && selectedEvent && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-900">Edit Event</h3>
              <button
                onClick={() => {
                  setShowEditModal(false);
                  setSelectedEvent(null);
                }}
                className="text-gray-400 hover:text-gray-600"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <EventForm
              event={selectedEvent}
              onSubmit={handleEditEvent}
              onCancel={() => {
                setShowEditModal(false);
                setSelectedEvent(null);
              }}
            />
          </div>
        </div>
      )}

      {showViewModal && selectedEvent && (
        <EventModal
          event={selectedEvent}
          mode="view"
          onClose={() => {
            setShowViewModal(false);
            setSelectedEvent(null);
          }}
          onEdit={(event) => {
            setShowViewModal(false);
            setSelectedEvent(event);
            setShowEditModal(true);
          }}
          onDelete={handleDeleteEvent}
        />
      )}
    </div>
  );
};

export default Events;
