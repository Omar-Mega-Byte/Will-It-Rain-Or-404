import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import EventCard from './EventCard';
import LoadingSpinner from './LoadingSpinner';
import eventService from '../services/eventService';

const UpcomingEvents = ({ 
  maxEvents = 3, 
  showViewAll = true, 
  compact = true 
}) => {
  const [events, setEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadUpcomingEvents();
  }, []);

  const loadUpcomingEvents = async () => {
    try {
      setIsLoading(true);
      setError(null);
      const response = await eventService.getUpcomingEvents();
      const upcomingEvents = response.data || [];
      
      // Limit the number of events displayed
      setEvents(upcomingEvents.slice(0, maxEvents));
    } catch (err) {
      setError(err.message || 'Failed to load upcoming events');
      setEvents([]);
    } finally {
      setIsLoading(false);
    }
  };

  const getTodaysEvents = () => {
    const today = new Date().toDateString();
    return events.filter(event => {
      const eventDate = new Date(event.startDate);
      return eventDate.toDateString() === today;
    });
  };

  const getThisWeekEvents = () => {
    const now = new Date();
    const weekFromNow = new Date(now.getTime() + 7 * 24 * 60 * 60 * 1000);
    
    return events.filter(event => {
      const eventDate = new Date(event.startDate);
      return eventDate >= now && eventDate <= weekFromNow;
    });
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Upcoming Events</h3>
        <div className="flex items-center justify-center py-8">
          <LoadingSpinner size="sm" />
          <span className="ml-2 text-gray-500">Loading events...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Upcoming Events</h3>
        <div className="text-center py-8">
          <svg className="w-12 h-12 text-red-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p className="text-red-600 text-sm">{error}</p>
        </div>
      </div>
    );
  }

  const todaysEvents = getTodaysEvents();
  const weekEvents = getThisWeekEvents();

  return (
    <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
      <div className="flex items-center justify-between mb-4">
        <h3 className="text-lg font-semibold text-gray-900">Upcoming Events</h3>
        {showViewAll && (
          <Link
            to="/events"
            className="text-sm text-blue-600 hover:text-blue-800 font-medium"
          >
            View All
          </Link>
        )}
      </div>

      {events.length === 0 ? (
        <div className="text-center py-8">
          <svg className="w-12 h-12 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
          </svg>
          <h4 className="text-sm font-medium text-gray-900 mb-2">No upcoming events</h4>
          <p className="text-sm text-gray-500 mb-4">You don't have any events scheduled.</p>
          <Link
            to="/events"
            className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700"
          >
            Create Event
          </Link>
        </div>
      ) : (
        <div className="space-y-4">
          {/* Today's Events */}
          {todaysEvents.length > 0 && (
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2 flex items-center">
                <span className="w-2 h-2 bg-red-500 rounded-full mr-2"></span>
                Today ({todaysEvents.length})
              </h4>
              <div className="space-y-2">
                {todaysEvents.map(event => (
                  <EventCard
                    key={event.id}
                    event={event}
                    compact={true}
                    showActions={false}
                  />
                ))}
              </div>
            </div>
          )}

          {/* This Week's Events */}
          {weekEvents.length > 0 && (
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2 flex items-center">
                <span className="w-2 h-2 bg-blue-500 rounded-full mr-2"></span>
                This Week ({weekEvents.length})
              </h4>
              <div className="space-y-2">
                {weekEvents.slice(0, maxEvents - todaysEvents.length).map(event => (
                  <EventCard
                    key={event.id}
                    event={event}
                    compact={true}
                    showActions={false}
                  />
                ))}
              </div>
            </div>
          )}

          {/* Other Upcoming Events */}
          {events.length > weekEvents.length && (
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-2 flex items-center">
                <span className="w-2 h-2 bg-gray-400 rounded-full mr-2"></span>
                Later
              </h4>
              <div className="space-y-2">
                {events
                  .filter(event => !weekEvents.includes(event))
                  .slice(0, Math.max(0, maxEvents - weekEvents.length))
                  .map(event => (
                    <EventCard
                      key={event.id}
                      event={event}
                      compact={true}
                      showActions={false}
                    />
                  ))}
              </div>
            </div>
          )}

          {/* Quick Actions */}
          <div className="pt-4 border-t border-gray-200">
            <div className="flex space-x-2">
              <Link
                to="/events"
                className="flex-1 text-center px-3 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                Manage Events
              </Link>
              <Link
                to="/calendar"
                className="flex-1 text-center px-3 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
              >
                View Calendar
              </Link>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UpcomingEvents;
