import React from 'react';
import Modal from './Modal';
import eventService from '../services/eventService';

const EventModal = ({
  isOpen,
  onClose,
  event,
  type = 'view', // 'view', 'delete', 'edit'
  onConfirm,
  isLoading = false
}) => {
  if (!event) return null;

  const getModalTitle = () => {
    switch (type) {
      case 'delete':
        return 'Delete Event';
      case 'edit':
        return 'Edit Event';
      default:
        return event.eventName;
    }
  };

  const getStatusBadgeClass = (status) => {
    const baseClass = 'px-3 py-1 rounded-full text-sm font-medium';
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

  const formatDuration = () => {
    if (!event.endDate) return 'No end time specified';
    
    const start = new Date(event.startDate);
    const end = new Date(event.endDate);
    const diffMs = end - start;
    const diffHours = Math.floor(diffMs / (1000 * 60 * 60));
    const diffMinutes = Math.floor((diffMs % (1000 * 60 * 60)) / (1000 * 60));
    
    if (diffHours > 0) {
      return diffMinutes > 0 ? `${diffHours} hours ${diffMinutes} minutes` : `${diffHours} hours`;
    }
    return `${diffMinutes} minutes`;
  };

  const isUpcoming = () => {
    return new Date(event.startDate) > new Date();
  };

  const isPast = () => {
    return new Date(event.endDate || event.startDate) < new Date();
  };

  const isActive = () => {
    const now = new Date();
    const start = new Date(event.startDate);
    const end = event.endDate ? new Date(event.endDate) : start;
    return now >= start && now <= end;
  };

  const renderDeleteConfirmation = () => (
    <div className="text-center py-4">
      <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
        <svg className="h-6 w-6 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
      </div>
      <h3 className="text-lg font-medium text-gray-900 mb-2">Delete Event</h3>
      <p className="text-sm text-gray-500 mb-4">
        Are you sure you want to delete "{event.eventName}"? This action cannot be undone.
      </p>
      <div className="bg-gray-50 rounded-lg p-3 mb-4">
        <div className="text-sm text-gray-600">
          <p><strong>Date:</strong> {eventService.formatDate(event.startDate)}</p>
          <p><strong>Type:</strong> {event.eventType}</p>
          {event.users && event.users.length > 0 && (
            <p><strong>Participants:</strong> {event.users.length}</p>
          )}
        </div>
      </div>
      <div className="flex justify-center space-x-4">
        <button
          onClick={onClose}
          className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50"
          disabled={isLoading}
        >
          Cancel
        </button>
        <button
          onClick={onConfirm}
          className="px-4 py-2 text-sm font-medium text-white bg-red-600 border border-transparent rounded-lg hover:bg-red-700 disabled:opacity-50"
          disabled={isLoading}
        >
          {isLoading ? 'Deleting...' : 'Delete Event'}
        </button>
      </div>
    </div>
  );

  const renderEventDetails = () => (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex items-start justify-between">
        <div className="flex items-start space-x-3">
          <div className="text-3xl">{eventService.getTypeIcon(event.eventType)}</div>
          <div>
            <h2 className="text-2xl font-bold text-gray-900 mb-2">{event.eventName}</h2>
            <div className="flex items-center space-x-3">
              <span className={getStatusBadgeClass(event.eventStatus)}>
                {event.eventStatus.replace('_', ' ')}
              </span>
              <span className={`inline-flex items-center px-3 py-1 rounded-full text-sm font-medium ${
                event.isOutdoor 
                  ? 'bg-green-100 text-green-800' 
                  : 'bg-blue-100 text-blue-800'
              }`}>
                {event.isOutdoor ? '🌤️ Outdoor' : '🏢 Indoor'}
              </span>
              {isActive() && (
                <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-red-100 text-red-800">
                  🔴 Live Now
                </span>
              )}
              {isUpcoming() && (
                <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-blue-100 text-blue-800">
                  ⏰ Upcoming
                </span>
              )}
              {isPast() && (
                <span className="inline-flex items-center px-3 py-1 rounded-full text-sm font-medium bg-gray-100 text-gray-800">
                  ✅ Past
                </span>
              )}
            </div>
          </div>
        </div>
      </div>

      {/* Description */}
      <div>
        <h3 className="text-lg font-medium text-gray-900 mb-2">Description</h3>
        <p className="text-gray-700 leading-relaxed">{event.eventDescription}</p>
      </div>

      {/* Event Details Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Date & Time */}
        <div className="space-y-4">
          <h3 className="text-lg font-medium text-gray-900">Date & Time</h3>
          <div className="space-y-3 text-sm">
            <div className="flex items-start space-x-3">
              <svg className="w-5 h-5 text-green-500 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
              <div>
                <p className="font-medium text-gray-900">Start</p>
                <p className="text-gray-600">{eventService.formatDate(event.startDate)}</p>
              </div>
            </div>
            
            {event.endDate && (
              <div className="flex items-start space-x-3">
                <svg className="w-5 h-5 text-red-500 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div>
                  <p className="font-medium text-gray-900">End</p>
                  <p className="text-gray-600">{eventService.formatDate(event.endDate)}</p>
                </div>
              </div>
            )}
            
            <div className="flex items-start space-x-3">
              <svg className="w-5 h-5 text-blue-500 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
              </svg>
              <div>
                <p className="font-medium text-gray-900">Duration</p>
                <p className="text-gray-600">{formatDuration()}</p>
              </div>
            </div>
          </div>
        </div>

        {/* Event Info */}
        <div className="space-y-4">
          <h3 className="text-lg font-medium text-gray-900">Event Information</h3>
          <div className="space-y-3 text-sm">
            <div className="flex items-center space-x-3">
              <svg className="w-5 h-5 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z" />
              </svg>
              <div>
                <span className="font-medium text-gray-900">Type: </span>
                <span className="text-gray-600">{event.eventType}</span>
              </div>
            </div>
            
            <div className="flex items-center space-x-3">
              <svg className="w-5 h-5 text-indigo-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.244-4.243a8 8 0 1111.314 0z" />
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 11a3 3 0 11-6 0 3 3 0 016 0z" />
              </svg>
              <div>
                <span className="font-medium text-gray-900">Location: </span>
                <span className="text-gray-600">{event.isOutdoor ? 'Outdoor Event' : 'Indoor Event'}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* Participants */}
      {event.users && event.users.length > 0 && (
        <div>
          <h3 className="text-lg font-medium text-gray-900 mb-3">
            Participants ({event.users.length})
          </h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
            {event.users.map((user) => (
              <div key={user.id} className="flex items-center space-x-3 p-3 bg-gray-50 rounded-lg">
                <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center text-white text-sm font-medium">
                  {user.firstName ? user.firstName.charAt(0) : user.username.charAt(0)}
                </div>
                <div>
                  <p className="text-sm font-medium text-gray-900">
                    {user.firstName && user.lastName 
                      ? `${user.firstName} ${user.lastName}` 
                      : user.username}
                  </p>
                  {user.firstName && user.lastName && (
                    <p className="text-xs text-gray-500">@{user.username}</p>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Timestamps */}
      <div className="pt-4 border-t border-gray-200">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-xs text-gray-500">
          <div>
            <span className="font-medium">Created:</span> {eventService.formatDate(event.createdAt)}
          </div>
          {event.updatedAt && event.updatedAt !== event.createdAt && (
            <div>
              <span className="font-medium">Last Updated:</span> {eventService.formatDate(event.updatedAt)}
            </div>
          )}
        </div>
      </div>
    </div>
  );

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title={getModalTitle()}
      size={type === 'delete' ? 'sm' : 'lg'}
    >
      {type === 'delete' ? renderDeleteConfirmation() : renderEventDetails()}
    </Modal>
  );
};

export default EventModal;
