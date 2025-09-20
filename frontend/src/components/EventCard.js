import React from 'react';
import eventService from '../services/eventService';

const EventCard = ({ 
  event, 
  onEdit, 
  onDelete, 
  onView, 
  showActions = true,
  compact = false 
}) => {
  const handleEdit = () => onEdit?.(event);
  const handleDelete = () => onDelete?.(event);
  const handleView = () => onView?.(event);

  const getStatusBadgeClass = (status) => {
    const baseClass = 'px-2.5 py-0.5 rounded-full text-xs font-medium capitalize';
    switch (status) {
      case 'SCHEDULED':
        return `${baseClass} bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200`;
      case 'IN_PROGRESS':
        return `${baseClass} bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200`;
      case 'COMPLETED':
        return `${baseClass} bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200`;
      case 'CANCELLED':
        return `${baseClass} bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200`;
      default:
        return `${baseClass} bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-200`;
    }
  };

  const getTypeIcon = (type) => eventService.getTypeIcon(type);

  const isUpcoming = () => new Date(event.startDate) > new Date();
  const isPast = () => new Date(event.endDate || event.startDate) < new Date();

  const formatDuration = () => {
    if (!event.endDate) return '';
    const start = new Date(event.startDate);
    const end = new Date(event.endDate);
    const diffMs = end - start;
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    
    if (diffHours > 0) {
      return diffMinutes > 0 ? `${diffHours}h ${diffMinutes}m` : `${diffHours}h`;
    }
    return `${diffMinutes}m`;
  };

  if (compact) {
    return (
      <div 
        className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 p-4 hover:shadow-lg transition-all duration-200 cursor-pointer group"
        onClick={handleView}
        role="button"
        tabIndex={0}
        onKeyPress={(e) => e.key === 'Enter' && handleView()}
        aria-label={`View ${event.eventName} details`}
      >
        <div className="flex items-center justify-between gap-3">
          <div className="flex items-center gap-3 flex-1 min-w-0">
            <span className="text-xl flex-shrink-0">{getTypeIcon(event.eventType)}</span>
            <div className="min-w-0 flex-1">
              <h4 className="text-sm font-semibold text-gray-900 dark:text-white truncate group-hover:text-blue-600 dark:group-hover:text-blue-400">
                {event.eventName}
              </h4>
              <div className="flex items-center gap-2 mt-1">
                <p className="text-xs text-gray-500 dark:text-gray-400">
                  {eventService.formatDate(event.startDate)}
                </p>
                {event.endDate && (
                  <span className="text-xs text-gray-400 dark:text-gray-500">
                    • {formatDuration()}
                  </span>
                )}
              </div>
            </div>
          </div>
          <span className={getStatusBadgeClass(event.eventStatus)}>
            {event.eventStatus.toLowerCase().replace('_', ' ')}
          </span>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white dark:bg-gray-800 rounded-xl border border-gray-200 dark:border-gray-700 shadow-sm hover:shadow-lg transition-all duration-200">
      <div className="p-5 sm:p-6">
        <div className="flex items-start justify-between gap-4 mb-4">
          <div className="flex items-start gap-4 flex-1">
            <div className="text-2xl flex-shrink-0">{getTypeIcon(event.eventType)}</div>
            <div className="min-w-0 flex-1">
              <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
                {event.eventName}
              </h3>
              <span className={getStatusBadgeClass(event.eventStatus)}>
                {event.eventStatus.toLowerCase().replace('_', ' ')}
              </span>
            </div>
          </div>
          
          {showActions && (
            <div className="flex items-center gap-2 flex-shrink-0">
              <button
                onClick={handleView}
                className="px-3 py-1 text-sm font-medium text-blue-600 hover:bg-blue-50 dark:text-blue-400 dark:hover:bg-blue-900/50 rounded-md transition-colors duration-150"
                title="View Details"
                aria-label={`View ${event.eventName} details`}
              >
                View
              </button>
              {(event.eventStatus === 'SCHEDULED' || event.eventStatus === 'IN_PROGRESS') && (
                <button
                  onClick={handleEdit}
                  className="px-3 py-1 text-sm font-medium text-indigo-600 hover:bg-indigo-50 dark:text-indigo-400 dark:hover:bg-indigo-900/50 rounded-md transition-colors duration-150"
                  title="Edit Event"
                  aria-label={`Edit ${event.eventName}`}
                >
                  Edit
                </button>
              )}
              <button
                onClick={handleDelete}
                className="px-3 py-1 text-sm font-medium text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-900/50 rounded-md transition-colors duration-150"
                title="Delete Event"
                aria-label={`Delete ${event.eventName}`}
              >
                Delete
              </button>
            </div>
          )}
        </div>

        <p className="text-gray-600 dark:text-gray-300 text-sm mb-4 line-clamp-3">
          {event.eventDescription || 'No description provided'}
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm">
          <div className="space-y-2">
            <div className="flex items-center text-gray-500 dark:text-gray-400">
              <svg className="w-4 h-4 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
              <span className="truncate">{eventService.formatDate(event.startDate)}</span>
            </div>
            
            {event.endDate && (
              <div className="flex items-center text-gray-500 dark:text-gray-400">
                <svg className="w-4 h-4 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <span>{formatDuration()}</span>
              </div>
            )}
          </div>

          <div className="space-y-2">
            <div className="flex items-center gap-2 flex-wrap">
              <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                event.isOutdoor 
                  ? 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200' 
                  : 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200'
              }`}>
                {event.isOutdoor ? '🌤️ Outdoor' : '🏢 Indoor'}
              </span>
              <span className="text-gray-400 dark:text-gray-500 text-xs capitalize">
                {event.eventType.toLowerCase()}
              </span>
            </div>

            {event.users && event.users.length > 0 && (
              <div className="flex items-center text-gray-500 dark:text-gray-400">
                <svg className="w-4 h-4 mr-2 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
                <span>{event.users.length} participant{event.users.length !== 1 ? 's' : ''}</span>
              </div>
            )}
          </div>
        </div>

        {(isUpcoming() || isPast() || event.endDate) && (
          <div className="mt-4 pt-3 border-t border-gray-100 dark:border-gray-700 flex items-center justify-between">
            {event.endDate && (
              <div className="text-xs text-gray-500 dark:text-gray-400">
                Ends: {eventService.formatDate(event.endDate)}
              </div>
            )}
            {(isUpcoming() || isPast()) && (
              <div className="text-xs text-gray-400 dark:text-gray-500 ml-auto">
                {isUpcoming() ? '⏰ Upcoming' : '✅ Past'}
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default EventCard;