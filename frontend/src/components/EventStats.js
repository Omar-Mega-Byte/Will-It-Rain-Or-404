import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import LoadingSpinner from './LoadingSpinner';
import StatCard from './StatCard';
import eventService from '../services/eventService';

const EventStats = ({ showDetailedStats = false }) => {
  const [stats, setStats] = useState(null);
  const [events, setEvents] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    loadEventStats();
  }, []);

  const loadEventStats = async () => {
    try {
      setIsLoading(true);
      setError(null);
      
      // Load both stats and events for detailed calculations
      const [statsResponse, eventsResponse] = await Promise.all([
        eventService.getUserEventStats(),
        eventService.getAllUserEvents()
      ]);
      
      setStats(statsResponse.data);
      setEvents(eventsResponse.data || []);
    } catch (err) {
      setError(err.message || 'Failed to load event statistics');
      setStats(null);
      setEvents([]);
    } finally {
      setIsLoading(false);
    }
  };

  // Calculate additional stats from events
  const getCalculatedStats = () => {
    if (!events.length) {
      return {
        totalEvents: 0,
        upcomingEvents: 0,
        eventsThisWeek: 0,
        eventsThisMonth: 0,
        completedEvents: 0,
        cancelledEvents: 0,
        inProgressEvents: 0,
        outdoorEvents: 0,
        indoorEvents: 0,
        eventsByType: {},
        averageEventsPerWeek: 0,
        nextEvent: null
      };
    }

    const now = new Date();
    const thisWeek = getThisWeekRange();
    const thisMonth = getThisMonthRange();

    const totalEvents = events.length;
    const upcomingEvents = events.filter(e => new Date(e.startDate) > now).length;
    const eventsThisWeek = events.filter(e => {
      const eventDate = new Date(e.startDate);
      return eventDate >= thisWeek.start && eventDate <= thisWeek.end;
    }).length;
    const eventsThisMonth = events.filter(e => {
      const eventDate = new Date(e.startDate);
      return eventDate >= thisMonth.start && eventDate <= thisMonth.end;
    }).length;

    const completedEvents = events.filter(e => e.eventStatus === 'COMPLETED').length;
    const cancelledEvents = events.filter(e => e.eventStatus === 'CANCELLED').length;
    const inProgressEvents = events.filter(e => e.eventStatus === 'IN_PROGRESS').length;
    const outdoorEvents = events.filter(e => e.isOutdoor).length;
    const indoorEvents = events.filter(e => !e.isOutdoor).length;

    // Group events by type
    const eventsByType = events.reduce((acc, event) => {
      const type = event.eventType;
      acc[type] = (acc[type] || 0) + 1;
      return acc;
    }, {});

    // Calculate average events per week (based on date range of events)
    let averageEventsPerWeek = 0;
    if (events.length > 1) {
      const sortedEvents = [...events].sort((a, b) => new Date(a.startDate) - new Date(b.startDate));
      const firstEvent = new Date(sortedEvents[0].startDate);
      const lastEvent = new Date(sortedEvents[sortedEvents.length - 1].startDate);
      const weeksDiff = Math.max(1, (lastEvent - firstEvent) / (7 * 24 * 60 * 60 * 1000));
      averageEventsPerWeek = Math.round((events.length / weeksDiff) * 10) / 10;
    }

    // Find next upcoming event
    const upcomingEventsList = events
      .filter(e => new Date(e.startDate) > now)
      .sort((a, b) => new Date(a.startDate) - new Date(b.startDate));
    const nextEvent = upcomingEventsList[0] || null;

    return {
      totalEvents,
      upcomingEvents,
      eventsThisWeek,
      eventsThisMonth,
      completedEvents,
      cancelledEvents,
      inProgressEvents,
      outdoorEvents,
      indoorEvents,
      eventsByType,
      averageEventsPerWeek,
      nextEvent
    };
  };

  const getThisWeekRange = () => {
    const now = new Date();
    const startOfWeek = new Date(now);
    startOfWeek.setDate(now.getDate() - now.getDay());
    startOfWeek.setHours(0, 0, 0, 0);
    
    const endOfWeek = new Date(startOfWeek);
    endOfWeek.setDate(startOfWeek.getDate() + 6);
    endOfWeek.setHours(23, 59, 59, 999);
    
    return { start: startOfWeek, end: endOfWeek };
  };

  const getThisMonthRange = () => {
    const now = new Date();
    const startOfMonth = new Date(now.getFullYear(), now.getMonth(), 1);
    const endOfMonth = new Date(now.getFullYear(), now.getMonth() + 1, 0);
    endOfMonth.setHours(23, 59, 59, 999);
    
    return { start: startOfMonth, end: endOfMonth };
  };

  const getMostPopularEventType = (eventsByType) => {
    if (!Object.keys(eventsByType).length) return 'None';
    
    const maxType = Object.entries(eventsByType)
      .reduce((max, [type, count]) => count > max.count ? { type, count } : max, { type: '', count: 0 });
    
    return maxType.type;
  };

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Event Statistics</h3>
        <div className="flex items-center justify-center py-8">
          <LoadingSpinner size="sm" />
          <span className="ml-2 text-gray-500">Loading statistics...</span>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
        <h3 className="text-lg font-semibold text-gray-900 mb-4">Event Statistics</h3>
        <div className="text-center py-8">
          <svg className="w-12 h-12 text-red-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          <p className="text-red-600 text-sm">{error}</p>
        </div>
      </div>
    );
  }

  const calculatedStats = getCalculatedStats();

  return (
    <div className="space-y-6">
      {/* Main Stats Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard
          title="Total Events"
          value={calculatedStats.totalEvents}
          icon="📅"
          color="blue"
          subtitle="All time"
        />
        <StatCard
          title="Upcoming"
          value={calculatedStats.upcomingEvents}
          icon="⏰"
          color="green"
          subtitle="Scheduled"
        />
        <StatCard
          title="This Week"
          value={calculatedStats.eventsThisWeek}
          icon="📆"
          color="yellow"
          subtitle="Current week"
        />
        <StatCard
          title="Completed"
          value={calculatedStats.completedEvents}
          icon="✅"
          color="purple"
          subtitle="Finished"
        />
      </div>

      {/* Detailed Stats */}
      {showDetailedStats && calculatedStats.totalEvents > 0 && (
        <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-6">
          <h3 className="text-lg font-semibold text-gray-900 mb-4">Detailed Statistics</h3>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {/* Event Status Breakdown */}
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-3">Event Status</h4>
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">Completed</span>
                  <span className="text-sm font-medium text-green-600">{calculatedStats.completedEvents}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">In Progress</span>
                  <span className="text-sm font-medium text-yellow-600">{calculatedStats.inProgressEvents}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">Cancelled</span>
                  <span className="text-sm font-medium text-red-600">{calculatedStats.cancelledEvents}</span>
                </div>
              </div>
            </div>

            {/* Location Type */}
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-3">Location Type</h4>
              <div className="space-y-2">
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">🌤️ Outdoor</span>
                  <span className="text-sm font-medium text-blue-600">{calculatedStats.outdoorEvents}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-sm text-gray-600">🏢 Indoor</span>
                  <span className="text-sm font-medium text-indigo-600">{calculatedStats.indoorEvents}</span>
                </div>
              </div>
            </div>

            {/* Event Types */}
            <div>
              <h4 className="text-sm font-medium text-gray-700 mb-3">Event Types</h4>
              <div className="space-y-2">
                {Object.entries(calculatedStats.eventsByType)
                  .sort((a, b) => b[1] - a[1])
                  .slice(0, 4)
                  .map(([type, count]) => (
                    <div key={type} className="flex justify-between items-center">
                      <span className="text-sm text-gray-600">
                        {eventService.getTypeIcon(type)} {type}
                      </span>
                      <span className="text-sm font-medium text-gray-900">{count}</span>
                    </div>
                  ))}
              </div>
            </div>
          </div>

          {/* Summary Stats */}
          <div className="mt-6 pt-6 border-t border-gray-200">
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-center">
              <div>
                <div className="text-2xl font-bold text-blue-600">{calculatedStats.eventsThisMonth}</div>
                <div className="text-sm text-gray-500">Events this month</div>
              </div>
              <div>
                <div className="text-2xl font-bold text-green-600">{calculatedStats.averageEventsPerWeek}</div>
                <div className="text-sm text-gray-500">Avg. events per week</div>
              </div>
              <div>
                <div className="text-2xl font-bold text-purple-600">
                  {getMostPopularEventType(calculatedStats.eventsByType)}
                </div>
                <div className="text-sm text-gray-500">Most popular type</div>
              </div>
            </div>
          </div>

          {/* Next Event */}
          {calculatedStats.nextEvent && (
            <div className="mt-6 pt-6 border-t border-gray-200">
              <h4 className="text-sm font-medium text-gray-700 mb-2">Next Event</h4>
              <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                <div className="flex items-center justify-between">
                  <div>
                    <p className="text-sm font-medium text-blue-900">
                      {calculatedStats.nextEvent.eventName}
                    </p>
                    <p className="text-xs text-blue-700">
                      {eventService.formatDate(calculatedStats.nextEvent.startDate)}
                    </p>
                  </div>
                  <div className="text-blue-600">
                    {eventService.getTypeIcon(calculatedStats.nextEvent.eventType)}
                  </div>
                </div>
              </div>
            </div>
          )}

          {/* Quick Actions */}
          <div className="mt-6 pt-6 border-t border-gray-200">
            <div className="flex space-x-3">
              <Link
                to="/events"
                className="flex-1 text-center px-4 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700"
              >
                Manage Events
              </Link>
              <Link
                to="/calendar"
                className="flex-1 text-center px-4 py-2 border border-gray-300 text-gray-700 rounded-lg font-medium hover:bg-gray-50"
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

export default EventStats;
