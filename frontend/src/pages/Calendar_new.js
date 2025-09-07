import React, { useState, useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../services/authService';
import eventService from '../services/eventService';
import EventCalendar from '../components/EventCalendar';
import EventForm from '../components/EventForm';
import EventModal from '../components/EventModal';

const Calendar = () => {
  const [user, setUser] = useState(null);
  const [events, setEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [showViewModal, setShowViewModal] = useState(false);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [selectedDate, setSelectedDate] = useState(null);
  const [currentDate, setCurrentDate] = useState(new Date());
  
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
    } catch (err) {
      setError('Failed to load events');
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
    setSelectedEvent(event);
    setShowViewModal(true);
  };

  const handleCreateEvent = async (eventData) => {
    try {
      // If a date was selected, use it as the start date
      if (selectedDate && !eventData.startDate) {
        const dateTime = new Date(selectedDate);
        dateTime.setHours(9, 0, 0, 0); // Default to 9 AM
        eventData.startDate = dateTime.toISOString();
        
        // Set end date to 1 hour later if not specified
        if (!eventData.endDate) {
          const endTime = new Date(dateTime);
          endTime.setHours(10, 0, 0, 0);
          eventData.endDate = endTime.toISOString();
        }
      }

      await eventService.createEvent(eventData);
      await loadEvents();
      setShowCreateModal(false);
      setSelectedDate(null);
    } catch (err) {
      throw new Error('Failed to create event');
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

  const handleLogout = () => {
    authService.logout();
    navigate('/');
  };

  // Get current month stats
  const getCurrentMonthStats = () => {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0);
    
    const monthEvents = events.filter(event => {
      const eventDate = new Date(event.startDate);
      return eventDate >= startOfMonth && eventDate <= endOfMonth;
    });

    const upcomingEvents = monthEvents.filter(event => 
      new Date(event.startDate) > now && event.eventStatus === 'SCHEDULED'
    );

    return {
      total: monthEvents.length,
      upcoming: upcomingEvents.length,
      completed: monthEvents.filter(e => e.eventStatus === 'COMPLETED').length,
      cancelled: monthEvents.filter(e => e.eventStatus === 'CANCELLED').length
    };
  };

  const monthStats = getCurrentMonthStats();

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
              <h1 className="text-xl font-bold text-gray-900">WeatherVision Calendar</h1>
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
                className="text-gray-700 hover:text-gray-900 px-3 py-2 rounded-md text-sm font-medium"
              >
                Events
              </Link>
              <Link
                to="/calendar"
                className="text-blue-600 hover:text-blue-700 px-3 py-2 rounded-md text-sm font-medium"
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
                <h1 className="text-2xl font-bold text-gray-900">Calendar</h1>
                <p className="mt-2 text-sm text-gray-700">
                  View and manage your events in calendar format
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

          <div className="grid grid-cols-1 lg:grid-cols-4 gap-6">
            
            {/* Calendar */}
            <div className="lg:col-span-3">
              {isLoading ? (
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-8">
                  <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-600 mx-auto"></div>
                    <p className="mt-4 text-gray-500">Loading calendar...</p>
                  </div>
                </div>
              ) : error ? (
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-8">
                  <div className="text-center">
                    <svg className="w-12 h-12 text-red-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <p className="text-red-600 mb-4">{error}</p>
                    <button
                      onClick={loadEvents}
                      className="text-blue-600 hover:text-blue-500"
                    >
                      Try again
                    </button>
                  </div>
                </div>
              ) : (
                <div className="bg-white rounded-lg shadow-sm border border-gray-200">
                  <EventCalendar
                    events={events}
                    onDateClick={handleDateClick}
                    onEventClick={handleEventClick}
                    currentDate={currentDate}
                    onCurrentDateChange={setCurrentDate}
                  />
                </div>
              )}
            </div>

            {/* Sidebar */}
            <div className="space-y-6">
              
              {/* Month Statistics */}
              <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">This Month</h3>
                <div className="space-y-4">
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600">Total Events</span>
                    <span className="text-lg font-semibold text-gray-900">{monthStats.total}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600">Upcoming</span>
                    <span className="text-lg font-semibold text-blue-600">{monthStats.upcoming}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600">Completed</span>
                    <span className="text-lg font-semibold text-green-600">{monthStats.completed}</span>
                  </div>
                  <div className="flex items-center justify-between">
                    <span className="text-sm text-gray-600">Cancelled</span>
                    <span className="text-lg font-semibold text-red-600">{monthStats.cancelled}</span>
                  </div>
                </div>
              </div>

              {/* Legend */}
              <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Legend</h3>
                <div className="space-y-3">
                  <div className="flex items-center">
                    <div className="w-4 h-4 bg-blue-500 rounded mr-3"></div>
                    <span className="text-sm text-gray-600">Scheduled</span>
                  </div>
                  <div className="flex items-center">
                    <div className="w-4 h-4 bg-yellow-500 rounded mr-3"></div>
                    <span className="text-sm text-gray-600">In Progress</span>
                  </div>
                  <div className="flex items-center">
                    <div className="w-4 h-4 bg-green-500 rounded mr-3"></div>
                    <span className="text-sm text-gray-600">Completed</span>
                  </div>
                  <div className="flex items-center">
                    <div className="w-4 h-4 bg-red-500 rounded mr-3"></div>
                    <span className="text-sm text-gray-600">Cancelled</span>
                  </div>
                </div>
              </div>

              {/* Quick Actions */}
              <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
                <h3 className="text-lg font-medium text-gray-900 mb-4">Quick Actions</h3>
                <div className="space-y-3">
                  <button
                    onClick={() => setShowCreateModal(true)}
                    className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 rounded-md"
                  >
                    📅 Create Event
                  </button>
                  <Link
                    to="/events"
                    className="block w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 rounded-md"
                  >
                    📋 View All Events
                  </Link>
                  <button
                    onClick={() => setCurrentDate(new Date())}
                    className="w-full text-left px-3 py-2 text-sm text-gray-700 hover:bg-gray-50 rounded-md"
                  >
                    📍 Go to Today
                  </button>
                </div>
              </div>

            </div>
          </div>
        </div>
      </main>

      {/* Create Event Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-gray-600 bg-opacity-50 overflow-y-auto h-full w-full z-50">
          <div className="relative top-20 mx-auto p-5 border w-11/12 md:w-3/4 lg:w-1/2 shadow-lg rounded-md bg-white">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-medium text-gray-900">
                Create New Event
                {selectedDate && (
                  <span className="text-sm text-gray-500 ml-2">
                    for {selectedDate.toLocaleDateString()}
                  </span>
                )}
              </h3>
              <button
                onClick={() => {
                  setShowCreateModal(false);
                  setSelectedDate(null);
                }}
                className="text-gray-400 hover:text-gray-600"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>
            <EventForm
              onSubmit={handleCreateEvent}
              onCancel={() => {
                setShowCreateModal(false);
                setSelectedDate(null);
              }}
              initialDate={selectedDate}
            />
          </div>
        </div>
      )}

      {/* View Event Modal */}
      {showViewModal && selectedEvent && (
        <EventModal
          event={selectedEvent}
          mode="view"
          onClose={() => {
            setShowViewModal(false);
            setSelectedEvent(null);
          }}
          onDelete={handleDeleteEvent}
        />
      )}
    </div>
  );
};

export default Calendar;
