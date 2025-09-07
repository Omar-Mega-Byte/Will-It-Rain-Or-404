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
  const handleEdit = () => {
    if (onEdit) onEdit(event);
  };

  const handleDelete = () => {
    if (onDelete) onDelete(event);
  };

  const handleView = () => {
    if (onView) onView(event);
  };

  const getStatusBadgeClass = (status) => {
    const baseClass = 'px-2 py-1 rounded-full text-xs font-medium';
    switch (status) {
      case 'SCHEDULED':
        return `${baseClass} bg-blue-100 text-blue-800`;
      case 'IN_PROGRESS':
        return `${baseClass} bg-yellow-100 text-yellow-800`;
      case 'COMPLETED':
        return `${baseClass} bg-green-100 text-green-800`;
      case 'CANCELLED':
        return `${baseClass} bg-red-100 text-red-800`;
      default:
        return `${baseClass} bg-gray-100 text-gray-800`;
    }
  };

  const getTypeIcon = (type) => {
    return eventService.getTypeIcon(type);
  };

  const isUpcoming = () => {
    return new Date(event.startDate) > new Date();
  };

  const isPast = () => {
    return new Date(event.endDate || event.startDate) < new Date();
  };

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
        className="bg-white rounded-lg border border-gray-200 p-3 hover:shadow-md transition-shadow cursor-pointer"
        onClick={handleView}
      >
        <div className="flex items-start justify-between">
          <div className="flex items-start space-x-2 flex-1">
            <span className="text-lg">{getTypeIcon(event.eventType)}</span>
            <div className="min-w-0 flex-1">
              <h4 className="text-sm font-medium text-gray-900 truncate">
                {event.eventName}
              </h4>
              <p className="text-xs text-gray-500 mt-1">
                {eventService.formatDate(event.startDate)}
              </p>
              {event.endDate && (
                <span className="text-xs text-gray-400">
                  {formatDuration()}
                </span>
              )}
            </div>
          </div>
          <span className={getStatusBadgeClass(event.eventStatus)}>
            {event.eventStatus.replace('_', ' ')}
          </span>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg border border-gray-200 shadow-sm hover:shadow-md transition-shadow">
      <div className="p-6">
        <div className="flex items-start justify-between mb-4">
          <div className="flex items-start space-x-3">
            <div className="text-2xl">{getTypeIcon(event.eventType)}</div>
            <div className="min-w-0 flex-1">
              <h3 className="text-lg font-semibold text-gray-900 mb-1">
                {event.eventName}
              </h3>
              <span className={getStatusBadgeClass(event.eventStatus)}>
                {event.eventStatus.replace('_', ' ')}
              </span>
            </div>
          </div>
          
          {showActions && (
            <div className="flex space-x-2 ml-4">
              <button
                onClick={handleView}
                className="text-blue-600 hover:text-blue-800 text-sm font-medium"
                title="View Details"
              >
                View
              </button>
              {(event.eventStatus === 'SCHEDULED' || event.eventStatus === 'IN_PROGRESS') && (
                <button
                  onClick={handleEdit}
                  className="text-indigo-600 hover:text-indigo-800 text-sm font-medium"
                  title="Edit Event"
                >
                  Edit
                </button>
              )}
              <button
                onClick={handleDelete}
                className="text-red-600 hover:text-red-800 text-sm font-medium"
                title="Delete Event"
              >
                Delete
              </button>
            </div>
          )}
        </div>

        <p className="text-gray-600 text-sm mb-4 line-clamp-2">
          {event.eventDescription}
        </p>

        <div className="space-y-2 text-sm">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <div className="flex items-center text-gray-500">
                <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                </svg>
                {eventService.formatDate(event.startDate)}
              </div>
              
              {event.endDate && (
                <div className="flex items-center text-gray-500">
                  <svg className="w-4 h-4 mr-1" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                  {formatDuration()}
                </div>
              )}
            </div>

            <div className="flex items-center space-x-2">
              <span className={`inline-flex items-center px-2 py-1 rounded-full text-xs font-medium ${
                event.isOutdoor 
                  ? 'bg-green-100 text-green-800' 
                  : 'bg-blue-100 text-blue-800'
              }`}>
                {event.isOutdoor ? '🌤️ Outdoor' : '🏢 Indoor'}
              </span>
              
              <span className="text-gray-400 text-xs">
                {event.eventType}
              </span>
            </div>
          </div>

          {event.users && event.users.length > 0 && (
            <div className="flex items-center justify-between pt-2 border-t border-gray-100">
              <div className="flex items-center space-x-2">
                <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
                <span className="text-gray-500 text-xs">
                  {event.users.length} participant{event.users.length !== 1 ? 's' : ''}
                </span>
              </div>

              {(isUpcoming() || isPast()) && (
                <div className="text-xs text-gray-400">
                  {isUpcoming() ? '⏰ Upcoming' : '✅ Past'}
                </div>
              )}
            </div>
          )}
        </div>

        {event.endDate && (
          <div className="mt-4 pt-3 border-t border-gray-100">
            <div className="text-xs text-gray-500">
              Ends: {eventService.formatDate(event.endDate)}
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default EventCard;
