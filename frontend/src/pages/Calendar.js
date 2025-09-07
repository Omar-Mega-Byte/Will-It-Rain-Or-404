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
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
            <svg className="w-12 h-12 text-red-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
            </svg>
            <h3 className="text-lg font-medium text-red-900 mb-2">Error Loading Calendar</h3>
            <p className="text-red-700 mb-4">{error}</p>
            <button
              onClick={loadEvents}
              className="bg-red-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-red-700 focus:outline-none focus:ring-2 focus:ring-red-500"
            >
              Try Again
            </button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
        {/* Header */}
        <div className="mb-8">
          <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between">
            <div>
              <h1 className="text-3xl font-bold text-gray-900">Calendar</h1>
              <p className="mt-2 text-gray-600">
                View and manage your events in calendar format
              </p>
            </div>
            
            <div className="mt-4 sm:mt-0">
              <button
                onClick={() => handleDateClick(new Date())}
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

        {/* Calendar Component */}
        <EventCalendar
          events={events}
          onEventClick={handleEventClick}
          onDateClick={handleDateClick}
          isLoading={isLoading}
          showCreateButton={false} // We have our own create button in the header
        />

        {/* Quick Stats */}
        {!isLoading && events.length > 0 && (
          <div className="mt-8 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
            <StatCard
              title="This Month"
              value={getEventsThisMonth()}
              icon="📅"
              color="blue"
            />
            <StatCard
              title="This Week"
              value={getEventsThisWeek()}
              icon="📆"
              color="green"
            />
            <StatCard
              title="Today"
              value={getEventsToday()}
              icon="⏰"
              color="yellow"
            />
            <StatCard
              title="Upcoming"
              value={getUpcomingEvents()}
              icon="🔜"
              color="purple"
            />
          </div>
        )}
      </div>

      {/* Create Event Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center p-4 z-50">
          <div className="bg-white rounded-lg max-w-4xl w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6 border-b border-gray-200">
              <div className="flex items-center justify-between">
                <h2 className="text-xl font-semibold text-gray-900">
                  Create New Event
                  {selectedDate && (
                    <span className="text-sm font-normal text-gray-500 ml-2">
                      for {selectedDate.toLocaleDateString()}
                    </span>
                  )}
                </h2>
                <button
                  onClick={handleCloseCreateModal}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>
            <div className="p-6">
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

export default Calendar;
