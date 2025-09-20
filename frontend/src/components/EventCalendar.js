import React, { useState, useEffect } from 'react';
import eventService from '../services/eventService';
import EventModal from './EventModal';
import LoadingSpinner from './LoadingSpinner';

const EventCalendar = ({ 
  events = [], 
  onEventClick, 
  onDateClick, 
  isLoading = false,
  showCreateButton = true 
}) => {
  const [currentDate, setCurrentDate] = useState(new Date());
  const [viewMode, setViewMode] = useState('month'); // 'month', 'week'
  const [selectedDate, setSelectedDate] = useState(null);
  const [selectedEvent, setSelectedEvent] = useState(null);
  const [showEventModal, setShowEventModal] = useState(false);

  const monthNames = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  const dayNames = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  // Get events for a specific date
  const getEventsForDate = (date) => {
    const dateStr = date.toDateString();
    return events.filter(event => {
      const eventStart = new Date(event.startDate);
      const eventEnd = event.endDate ? new Date(event.endDate) : eventStart;
      
      // Check if the date falls within the event duration
      const eventStartStr = eventStart.toDateString();
      const eventEndStr = eventEnd.toDateString();
      
      return dateStr >= eventStartStr && dateStr <= eventEndStr;
    });
  };

  // Generate calendar days for month view
  const generateMonthDays = () => {
    const year = currentDate.getFullYear();
    const month = currentDate.getMonth();
    
    const firstDay = new Date(year, month, 1);
    const lastDay = new Date(year, month + 1, 0);
    const startDate = new Date(firstDay);
    startDate.setDate(startDate.getDate() - firstDay.getDay());
    
    const days = [];
    const current = new Date(startDate);
    
    // Generate 6 weeks (42 days) to fill the calendar grid
    for (let i = 0; i < 42; i++) {
      days.push(new Date(current));
      current.setDate(current.getDate() + 1);
    }
    
    return days;
  };

  // Generate week days for week view
  const generateWeekDays = () => {
    const startOfWeek = new Date(currentDate);
    const day = startOfWeek.getDay();
    startOfWeek.setDate(startOfWeek.getDate() - day);
    
    const days = [];
    for (let i = 0; i < 7; i++) {
      const date = new Date(startOfWeek);
      date.setDate(startOfWeek.getDate() + i);
      days.push(date);
    }
    
    return days;
  };

  // Navigation functions
  const navigatePrevious = () => {
    const newDate = new Date(currentDate);
    if (viewMode === 'month') {
      newDate.setMonth(newDate.getMonth() - 1);
    } else {
      newDate.setDate(newDate.getDate() - 7);
    }
    setCurrentDate(newDate);
  };

  const navigateNext = () => {
    const newDate = new Date(currentDate);
    if (viewMode === 'month') {
      newDate.setMonth(newDate.getMonth() + 1);
    } else {
      newDate.setDate(newDate.getDate() + 7);
    }
    setCurrentDate(newDate);
  };

  const navigateToday = () => {
    setCurrentDate(new Date());
  };

  // Event handlers
  const handleDateClick = (date) => {
    setSelectedDate(date);
    if (onDateClick) {
      onDateClick(date);
    }
  };

  const handleEventClick = (event, e) => {
    e.stopPropagation();
    setSelectedEvent(event);
    if (onEventClick) {
      onEventClick(event);
    } else {
      setShowEventModal(true);
    }
  };

  // Helper functions
  const isToday = (date) => {
    const today = new Date();
    return date.toDateString() === today.toDateString();
  };

  const isCurrentMonth = (date) => {
    return date.getMonth() === currentDate.getMonth();
  };

  const isSelected = (date) => {
    return selectedDate && date.toDateString() === selectedDate.toDateString();
  };

  const getEventStatusColor = (status) => {
    const colors = {
      SCHEDULED: 'bg-blue-500',
      IN_PROGRESS: 'bg-yellow-500',
      COMPLETED: 'bg-green-500',
      CANCELLED: 'bg-red-500'
    };
    return colors[status] || 'bg-gray-500';
  };

  const formatDateRange = () => {
    if (viewMode === 'month') {
      return `${monthNames[currentDate.getMonth()]} ${currentDate.getFullYear()}`;
    } else {
      const weekDays = generateWeekDays();
      const start = weekDays[0];
      const end = weekDays[6];
      
      if (start.getMonth() === end.getMonth()) {
        return `${monthNames[start.getMonth()]} ${start.getDate()} - ${end.getDate()}, ${start.getFullYear()}`;
      } else {
        return `${monthNames[start.getMonth()]} ${start.getDate()} - ${monthNames[end.getMonth()]} ${end.getDate()}, ${start.getFullYear()}`;
      }
    }
  };

  const renderMonthView = () => {
    const days = generateMonthDays();

    return (
      <div className="grid grid-cols-7 gap-1">
        {/* Day headers */}
        {dayNames.map(day => (
          <div key={day} className="p-2 text-center text-sm font-medium text-gray-500 bg-gray-50">
            {day}
          </div>
        ))}
        
        {/* Calendar days */}
        {days.map((date, index) => {
          const dayEvents = getEventsForDate(date);
          const isCurrentMonthDay = isCurrentMonth(date);
          
          return (
            <div
              key={index}
              onClick={() => handleDateClick(date)}
              className={`min-h-24 p-1 border border-gray-200 cursor-pointer hover:bg-gray-50 ${
                isCurrentMonthDay ? 'bg-white' : 'bg-gray-100'
              } ${isSelected(date) ? 'ring-2 ring-blue-500' : ''}`}
            >
              <div className={`text-sm font-medium mb-1 ${
                isToday(date) 
                  ? 'bg-blue-600 text-white w-6 h-6 rounded-full flex items-center justify-center' 
                  : isCurrentMonthDay 
                    ? 'text-gray-900' 
                    : 'text-gray-400'
              }`}>
                {date.getDate()}
              </div>
              
              {/* Events for this day */}
              <div className="space-y-1">
                {dayEvents.slice(0, 3).map((event) => (
                  <div
                    key={event.id || event.eventId}
                    onClick={(e) => handleEventClick(event, e)}
                    className={`text-xs p-1 rounded text-white truncate cursor-pointer hover:opacity-80 ${getEventStatusColor(event.eventStatus)}`}
                    title={`${event.eventName} - ${eventService.formatDate(event.startDate)}`}
                  >
                    {event.eventName}
                  </div>
                ))}
                {dayEvents.length > 3 && (
                  <div className="text-xs text-gray-500 font-medium">
                    +{dayEvents.length - 3} more
                  </div>
                )}
              </div>
            </div>
          );
        })}
      </div>
    );
  };

  const renderWeekView = () => {
    const days = generateWeekDays();
    const hours = Array.from({ length: 24 }, (_, i) => i);

    return (
      <div className="grid grid-cols-8 gap-1">
        {/* Time column header */}
        <div className="p-2 text-center text-sm font-medium text-gray-500 bg-gray-50">
          Time
        </div>
        
        {/* Day headers */}
        {days.map(day => (
          <div key={day.toISOString()} className="p-2 text-center text-sm font-medium text-gray-500 bg-gray-50">
            <div>{dayNames[day.getDay()]}</div>
            <div className={`text-lg ${isToday(day) ? 'bg-blue-600 text-white w-8 h-8 rounded-full flex items-center justify-center mx-auto' : 'text-gray-900'}`}>
              {day.getDate()}
            </div>
          </div>
        ))}
        
        {/* Time slots */}
        {hours.map(hour => (
          <React.Fragment key={hour}>
            {/* Time label */}
            <div className="p-2 text-xs text-gray-500 border-r border-gray-200 text-center">
              {hour.toString().padStart(2, '0')}:00
            </div>
            
            {/* Day columns */}
            {days.map(day => {
              const hourEvents = getEventsForDate(day).filter(event => {
                const eventStart = new Date(event.startDate);
                return eventStart.getHours() === hour;
              });
              
              return (
                <div
                  key={`${day.toISOString()}-${hour}`}
                  onClick={() => handleDateClick(day)}
                  className="min-h-12 p-1 border border-gray-200 cursor-pointer hover:bg-gray-50"
                >
                  {hourEvents.map(event => (
                    <div
                      key={event.id || event.eventId}
                      onClick={(e) => handleEventClick(event, e)}
                      className={`text-xs p-1 rounded text-white mb-1 cursor-pointer hover:opacity-80 ${getEventStatusColor(event.eventStatus)}`}
                      title={`${event.eventName} - ${eventService.formatDate(event.startDate)}`}
                    >
                      {event.eventName}
                    </div>
                  ))}
                </div>
              );
            })}
          </React.Fragment>
        ))}
      </div>
    );
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <LoadingSpinner size="lg" />
        <span className="ml-2 text-gray-500">Loading calendar...</span>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200">
      {/* Calendar Header */}
      <div className="p-4 border-b border-gray-200">
        <div className="flex items-center justify-between">
          <div className="flex items-center space-x-4">
            <h2 className="text-xl font-semibold text-gray-900">
              {formatDateRange()}
            </h2>
            
            <div className="flex items-center space-x-2">
              <button
                onClick={navigatePrevious}
                className="p-2 text-gray-400 hover:text-gray-600 focus:outline-none"
                title="Previous"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 19l-7-7 7-7" />
                </svg>
              </button>
              
              <button
                onClick={navigateToday}
                className="px-3 py-1 text-sm font-medium text-blue-600 hover:text-blue-800 focus:outline-none"
              >
                Today
              </button>
              
              <button
                onClick={navigateNext}
                className="p-2 text-gray-400 hover:text-gray-600 focus:outline-none"
                title="Next"
              >
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5l7 7-7 7" />
                </svg>
              </button>
            </div>
          </div>

          <div className="flex items-center space-x-4">
            {/* View Mode Toggle */}
            <div className="flex items-center bg-gray-100 rounded-lg">
              <button
                onClick={() => setViewMode('month')}
                className={`px-3 py-1 text-sm font-medium rounded-l-lg ${
                  viewMode === 'month'
                    ? 'bg-blue-600 text-white'
                    : 'text-gray-700 hover:bg-gray-200'
                }`}
              >
                Month
              </button>
              <button
                onClick={() => setViewMode('week')}
                className={`px-3 py-1 text-sm font-medium rounded-r-lg ${
                  viewMode === 'week'
                    ? 'bg-blue-600 text-white'
                    : 'text-gray-700 hover:bg-gray-200'
                }`}
              >
                Week
              </button>
            </div>

            {/* Create Event Button */}
            {showCreateButton && (
              <button
                onClick={() => handleDateClick(selectedDate || new Date())}
                className="bg-blue-600 text-white px-4 py-2 rounded-lg font-medium hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 text-sm"
              >
                Create Event
              </button>
            )}
          </div>
        </div>

        {/* Legend */}
        <div className="mt-4 flex items-center space-x-6 text-xs">
          <div className="flex items-center space-x-2">
            <div className="w-3 h-3 bg-blue-500 rounded"></div>
            <span className="text-gray-600">Scheduled</span>
          </div>
          <div className="flex items-center space-x-2">
            <div className="w-3 h-3 bg-yellow-500 rounded"></div>
            <span className="text-gray-600">In Progress</span>
          </div>
          <div className="flex items-center space-x-2">
            <div className="w-3 h-3 bg-green-500 rounded"></div>
            <span className="text-gray-600">Completed</span>
          </div>
          <div className="flex items-center space-x-2">
            <div className="w-3 h-3 bg-red-500 rounded"></div>
            <span className="text-gray-600">Cancelled</span>
          </div>
        </div>
      </div>

      {/* Calendar Body */}
      <div className="p-4">
        {viewMode === 'month' ? renderMonthView() : renderWeekView()}
      </div>

      {/* Event Details Modal */}
      <EventModal
        isOpen={showEventModal}
        onClose={() => {
          setShowEventModal(false);
          setSelectedEvent(null);
        }}
        event={selectedEvent}
        type="view"
      />
    </div>
  );
};

export default EventCalendar;
