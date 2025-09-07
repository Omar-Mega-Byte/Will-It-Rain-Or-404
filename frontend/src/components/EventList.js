import React, { useState } from 'react';
import EventCard from './EventCard';
import Pagination from './Pagination';
import LoadingSpinner from './LoadingSpinner';

const EventList = ({
  events = [],
  isLoading = false,
  error = null,
  onEdit,
  onDelete,
  onView,
  showActions = true,
  compact = false,
  showPagination = false,
  currentPage = 0,
  totalPages = 0,
  onPageChange,
  emptyMessage = 'No events found',
  title
}) => {
  const [sortBy, setSortBy] = useState('startDate');
  const [sortOrder, setSortOrder] = useState('asc');
  const [filterStatus, setFilterStatus] = useState('all');

  // Sort events
  const sortedEvents = React.useMemo(() => {
    if (!events || events.length === 0) return [];

    let filtered = events;

    // Filter by status
    if (filterStatus !== 'all') {
      filtered = events.filter(event => event.eventStatus === filterStatus);
    }

    // Sort events
    return [...filtered].sort((a, b) => {
      let aValue, bValue;

      switch (sortBy) {
        case 'startDate':
          aValue = new Date(a.startDate);
          bValue = new Date(b.startDate);
          break;
        case 'eventName':
          aValue = a.eventName.toLowerCase();
          bValue = b.eventName.toLowerCase();
          break;
        case 'eventType':
          aValue = a.eventType;
          bValue = b.eventType;
          break;
        case 'eventStatus':
          aValue = a.eventStatus;
          bValue = b.eventStatus;
          break;
        default:
          aValue = new Date(a.startDate);
          bValue = new Date(b.startDate);
      }

      if (aValue < bValue) return sortOrder === 'asc' ? -1 : 1;
      if (aValue > bValue) return sortOrder === 'asc' ? 1 : -1;
      return 0;
    });
  }, [events, sortBy, sortOrder, filterStatus]);

  const handleSort = (field) => {
    if (sortBy === field) {
      setSortOrder(sortOrder === 'asc' ? 'desc' : 'asc');
    } else {
      setSortBy(field);
      setSortOrder('asc');
    }
  };

  const getSortIcon = (field) => {
    if (sortBy !== field) {
      return (
        <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16V4m0 0L3 8m4-4l4 4m6 0v12m0 0l4-4m-4 4l-4-4" />
        </svg>
      );
    }

    return sortOrder === 'asc' ? (
      <svg className="w-4 h-4 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4h13M3 8h9m-9 4h6m4 0l4-4m0 0l4 4m-4-4v12" />
      </svg>
    ) : (
      <svg className="w-4 h-4 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 4h13M3 8h9m-9 4h9m5-4v12m0 0l-4-4m4 4l4-4" />
      </svg>
    );
  };

  if (error) {
    return (
      <div className="bg-red-50 border border-red-200 rounded-lg p-6 text-center">
        <svg className="w-12 h-12 text-red-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <h3 className="text-lg font-medium text-red-900 mb-2">Error Loading Events</h3>
        <p className="text-red-700">{error}</p>
      </div>
    );
  }

  if (isLoading) {
    return (
      <div className="text-center py-12">
        <LoadingSpinner size="lg" />
        <p className="mt-4 text-gray-500">Loading events...</p>
      </div>
    );
  }

  if (sortedEvents.length === 0) {
    return (
      <div className="text-center py-12">
        <svg className="w-16 h-16 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
        </svg>
        <h3 className="text-lg font-medium text-gray-900 mb-2">No Events</h3>
        <p className="text-gray-500">{emptyMessage}</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {/* Header with Title and Controls */}
      {(title || (!compact && events.length > 1)) && (
        <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between space-y-4 sm:space-y-0">
          {title && (
            <h2 className="text-xl font-semibold text-gray-900">{title}</h2>
          )}
          
          {!compact && events.length > 1 && (
            <div className="flex flex-col sm:flex-row space-y-2 sm:space-y-0 sm:space-x-4">
              {/* Status Filter */}
              <div className="flex items-center space-x-2">
                <label className="text-sm font-medium text-gray-700">Status:</label>
                <select
                  value={filterStatus}
                  onChange={(e) => setFilterStatus(e.target.value)}
                  className="px-3 py-1 border border-gray-300 rounded-md text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                >
                  <option value="all">All</option>
                  <option value="SCHEDULED">Scheduled</option>
                  <option value="IN_PROGRESS">In Progress</option>
                  <option value="COMPLETED">Completed</option>
                  <option value="CANCELLED">Cancelled</option>
                </select>
              </div>

              {/* Sort Controls */}
              <div className="flex items-center space-x-2">
                <label className="text-sm font-medium text-gray-700">Sort by:</label>
                <div className="flex space-x-1">
                  <button
                    onClick={() => handleSort('startDate')}
                    className={`px-3 py-1 text-sm rounded-md border flex items-center space-x-1 ${
                      sortBy === 'startDate'
                        ? 'bg-blue-100 border-blue-300 text-blue-700'
                        : 'bg-white border-gray-300 text-gray-700 hover:bg-gray-50'
                    }`}
                  >
                    <span>Date</span>
                    {getSortIcon('startDate')}
                  </button>
                  <button
                    onClick={() => handleSort('eventName')}
                    className={`px-3 py-1 text-sm rounded-md border flex items-center space-x-1 ${
                      sortBy === 'eventName'
                        ? 'bg-blue-100 border-blue-300 text-blue-700'
                        : 'bg-white border-gray-300 text-gray-700 hover:bg-gray-50'
                    }`}
                  >
                    <span>Name</span>
                    {getSortIcon('eventName')}
                  </button>
                  <button
                    onClick={() => handleSort('eventType')}
                    className={`px-3 py-1 text-sm rounded-md border flex items-center space-x-1 ${
                      sortBy === 'eventType'
                        ? 'bg-blue-100 border-blue-300 text-blue-700'
                        : 'bg-white border-gray-300 text-gray-700 hover:bg-gray-50'
                    }`}
                  >
                    <span>Type</span>
                    {getSortIcon('eventType')}
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      )}

      {/* Events Display */}
      <div className={`${
        compact 
          ? 'space-y-2' 
          : 'grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-6'
      }`}>
        {sortedEvents.map((event) => (
          <EventCard
            key={event.id}
            event={event}
            onEdit={onEdit}
            onDelete={onDelete}
            onView={onView}
            showActions={showActions}
            compact={compact}
          />
        ))}
      </div>

      {/* Results Info */}
      {!compact && (
        <div className="flex justify-between items-center text-sm text-gray-500 border-t pt-4">
          <span>
            Showing {sortedEvents.length} of {events.length} event{events.length !== 1 ? 's' : ''}
            {filterStatus !== 'all' && ` (filtered by ${filterStatus.toLowerCase().replace('_', ' ')})`}
          </span>
          
          {sortBy && (
            <span>
              Sorted by {sortBy.replace(/([A-Z])/g, ' $1').toLowerCase()} ({sortOrder}ending)
            </span>
          )}
        </div>
      )}

      {/* Pagination */}
      {showPagination && totalPages > 1 && (
        <div className="flex justify-center mt-8">
          <Pagination
            currentPage={currentPage}
            totalPages={totalPages}
            onPageChange={onPageChange}
          />
        </div>
      )}
    </div>
  );
};

export default EventList;
