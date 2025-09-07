import React, { useState, useEffect } from 'react';
import eventService from '../services/eventService';
import LoadingSpinner from './LoadingSpinner';

const EventForm = ({ 
  event = null, 
  onSubmit, 
  onCancel, 
  isLoading = false 
}) => {
  const [formData, setFormData] = useState({
    eventName: '',
    eventDescription: '',
    eventType: '',
    startDate: '',
    endDate: '',
    isOutdoor: true,
    eventStatus: 'SCHEDULED',
    userIds: []
  });

  const [errors, setErrors] = useState({});
  const [isCheckingConflicts, setIsCheckingConflicts] = useState(false);
  const [conflicts, setConflicts] = useState([]);
  const [isNameAvailable, setIsNameAvailable] = useState(true);

  const eventTypes = eventService.getEventTypes();
  const eventStatuses = eventService.getEventStatuses();

  useEffect(() => {
    if (event) {
      setFormData({
        eventName: event.eventName || '',
        eventDescription: event.eventDescription || '',
        eventType: event.eventType || '',
        startDate: eventService.formatDateForInput(event.startDate) || '',
        endDate: eventService.formatDateForInput(event.endDate) || '',
        isOutdoor: event.isOutdoor !== undefined ? event.isOutdoor : true,
        eventStatus: event.eventStatus || 'SCHEDULED',
        userIds: event.users ? event.users.map(user => user.id) : []
      });
    }
  }, [event]);

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    const newValue = type === 'checkbox' ? checked : value;
    
    setFormData(prev => ({
      ...prev,
      [name]: newValue
    }));

    // Clear error when user starts typing
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }

    // Check name availability for new events
    if (name === 'eventName' && value.trim() && !event) {
      debounceNameCheck(value.trim());
    }

    // Check for conflicts when dates change
    if ((name === 'startDate' || name === 'endDate') && value) {
      const updatedFormData = { ...formData, [name]: value };
      if (updatedFormData.startDate && updatedFormData.endDate) {
        debounceConflictCheck(updatedFormData.startDate, updatedFormData.endDate);
      }
    }
  };

  // Debounce name availability check
  const debounceNameCheck = React.useCallback(
    debounce(async (name) => {
      try {
        const response = await eventService.checkEventNameAvailability(name);
        setIsNameAvailable(response.data);
      } catch (error) {
        console.error('Error checking name availability:', error);
      }
    }, 500),
    []
  );

  // Debounce conflict check
  const debounceConflictCheck = React.useCallback(
    debounce(async (startDate, endDate) => {
      setIsCheckingConflicts(true);
      try {
        const response = await eventService.getConflictingEvents(startDate, endDate);
        const conflictingEvents = response.data || [];
        
        // Filter out current event if editing
        const filteredConflicts = event 
          ? conflictingEvents.filter(conflict => conflict.id !== event.id)
          : conflictingEvents;
          
        setConflicts(filteredConflicts);
      } catch (error) {
        console.error('Error checking conflicts:', error);
        setConflicts([]);
      } finally {
        setIsCheckingConflicts(false);
      }
    }, 500),
    [event]
  );

  const validateForm = () => {
    const validation = eventService.validateEventData(formData);
    
    // Add name availability check for new events
    if (!event && !isNameAvailable) {
      validation.errors.eventName = 'Event name is already taken';
      validation.isValid = false;
    }

    setErrors(validation.errors);
    return validation.isValid;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (!validateForm()) {
      return;
    }

    // Prepare data for submission
    const submitData = {
      ...formData,
      startDate: new Date(formData.startDate).toISOString(),
      endDate: formData.endDate ? new Date(formData.endDate).toISOString() : null
    };

    try {
      await onSubmit(submitData);
    } catch (error) {
      // Handle submission errors
      if (error.data && error.data.errors) {
        setErrors(error.data.errors);
      }
    }
  };

  const handleCancel = () => {
    if (onCancel) {
      onCancel();
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="bg-white">
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {/* Event Name */}
          <div className="md:col-span-2">
            <label htmlFor="eventName" className="block text-sm font-medium text-gray-700 mb-2">
              Event Name *
            </label>
            <div className="relative">
              <input
                type="text"
                id="eventName"
                name="eventName"
                value={formData.eventName}
                onChange={handleInputChange}
                className={`w-full px-3 py-2 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                  errors.eventName ? 'border-red-500' : 'border-gray-300'
                } ${!event && formData.eventName && !isNameAvailable ? 'border-red-500' : ''}`}
                placeholder="Enter event name"
                maxLength={255}
              />
              {!event && formData.eventName && (
                <div className="absolute right-3 top-2">
                  {isNameAvailable ? (
                    <span className="text-green-500 text-sm">✓</span>
                  ) : (
                    <span className="text-red-500 text-sm">✗</span>
                  )}
                </div>
              )}
            </div>
            {errors.eventName && (
              <p className="mt-1 text-sm text-red-600">{errors.eventName}</p>
            )}
          </div>

          {/* Event Type */}
          <div>
            <label htmlFor="eventType" className="block text-sm font-medium text-gray-700 mb-2">
              Event Type *
            </label>
            <select
              id="eventType"
              name="eventType"
              value={formData.eventType}
              onChange={handleInputChange}
              className={`w-full px-3 py-2 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                errors.eventType ? 'border-red-500' : 'border-gray-300'
              }`}
            >
              <option value="">Select event type</option>
              {eventTypes.map(type => (
                <option key={type.value} value={type.value}>
                  {eventService.getTypeIcon(type.value)} {type.label}
                </option>
              ))}
            </select>
            {errors.eventType && (
              <p className="mt-1 text-sm text-red-600">{errors.eventType}</p>
            )}
          </div>

          {/* Event Status */}
          <div>
            <label htmlFor="eventStatus" className="block text-sm font-medium text-gray-700 mb-2">
              Status
            </label>
            <select
              id="eventStatus"
              name="eventStatus"
              value={formData.eventStatus}
              onChange={handleInputChange}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              {eventStatuses.map(status => (
                <option key={status.value} value={status.value}>
                  {status.label}
                </option>
              ))}
            </select>
          </div>

          {/* Start Date */}
          <div>
            <label htmlFor="startDate" className="block text-sm font-medium text-gray-700 mb-2">
              Start Date & Time *
            </label>
            <input
              type="datetime-local"
              id="startDate"
              name="startDate"
              value={formData.startDate}
              onChange={handleInputChange}
              className={`w-full px-3 py-2 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                errors.startDate ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {errors.startDate && (
              <p className="mt-1 text-sm text-red-600">{errors.startDate}</p>
            )}
          </div>

          {/* End Date */}
          <div>
            <label htmlFor="endDate" className="block text-sm font-medium text-gray-700 mb-2">
              End Date & Time
            </label>
            <input
              type="datetime-local"
              id="endDate"
              name="endDate"
              value={formData.endDate}
              onChange={handleInputChange}
              className={`w-full px-3 py-2 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                errors.endDate ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {errors.endDate && (
              <p className="mt-1 text-sm text-red-600">{errors.endDate}</p>
            )}
          </div>

          {/* Outdoor/Indoor */}
          <div className="md:col-span-2">
            <label className="block text-sm font-medium text-gray-700 mb-2">
              Event Location Type *
            </label>
            <div className="flex space-x-4">
              <label className="flex items-center">
                <input
                  type="radio"
                  name="isOutdoor"
                  value={true}
                  checked={formData.isOutdoor === true}
                  onChange={() => setFormData(prev => ({ ...prev, isOutdoor: true }))}
                  className="mr-2"
                />
                🌤️ Outdoor
              </label>
              <label className="flex items-center">
                <input
                  type="radio"
                  name="isOutdoor"
                  value={false}
                  checked={formData.isOutdoor === false}
                  onChange={() => setFormData(prev => ({ ...prev, isOutdoor: false }))}
                  className="mr-2"
                />
                🏢 Indoor
              </label>
            </div>
            {errors.isOutdoor && (
              <p className="mt-1 text-sm text-red-600">{errors.isOutdoor}</p>
            )}
          </div>

          {/* Event Description */}
          <div className="md:col-span-2">
            <label htmlFor="eventDescription" className="block text-sm font-medium text-gray-700 mb-2">
              Event Description *
            </label>
            <textarea
              id="eventDescription"
              name="eventDescription"
              value={formData.eventDescription}
              onChange={handleInputChange}
              rows={4}
              className={`w-full px-3 py-2 border rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-blue-500 ${
                errors.eventDescription ? 'border-red-500' : 'border-gray-300'
              }`}
              placeholder="Describe your event..."
              maxLength={1000}
            />
            <div className="flex justify-between mt-1">
              {errors.eventDescription ? (
                <p className="text-sm text-red-600">{errors.eventDescription}</p>
              ) : (
                <span></span>
              )}
              <span className="text-sm text-gray-500">
                {formData.eventDescription.length}/1000
              </span>
            </div>
          </div>
        </div>

        {/* Conflict Warning */}
        {(isCheckingConflicts || conflicts.length > 0) && (
          <div className="mt-6 p-4 bg-yellow-50 border border-yellow-200 rounded-lg">
            <div className="flex items-start">
              <svg className="w-5 h-5 text-yellow-400 mr-2 mt-0.5" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
              <div>
                {isCheckingConflicts ? (
                  <p className="text-yellow-800">Checking for conflicts...</p>
                ) : conflicts.length > 0 ? (
                  <div>
                    <p className="text-yellow-800 font-medium">
                      ⚠️ Scheduling Conflict Detected
                    </p>
                    <p className="text-yellow-700 text-sm mt-1">
                      You have {conflicts.length} other event{conflicts.length > 1 ? 's' : ''} during this time:
                    </p>
                    <ul className="list-disc list-inside text-yellow-700 text-sm mt-2">
                      {conflicts.map(conflict => (
                        <li key={conflict.id}>
                          {conflict.eventName} ({eventService.formatDate(conflict.startDate)})
                        </li>
                      ))}
                    </ul>
                  </div>
                ) : null}
              </div>
            </div>
          </div>
        )}

        {/* Form Actions */}
        <div className="flex justify-end space-x-4 pt-6 border-t border-gray-200">
          <button
            type="button"
            onClick={handleCancel}
            className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
            disabled={isLoading}
          >
            Cancel
          </button>
          <button
            type="submit"
            disabled={isLoading || (conflicts.length > 0)}
            className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed flex items-center"
          >
            {isLoading && <LoadingSpinner size="sm" className="mr-2" />}
            {event ? 'Update Event' : 'Create Event'}
          </button>
        </div>
      </div>
    </form>
  );
};

// Debounce utility function
function debounce(func, wait) {
  let timeout;
  return function executedFunction(...args) {
    const later = () => {
      clearTimeout(timeout);
      func(...args);
    };
    clearTimeout(timeout);
    timeout = setTimeout(later, wait);
  };
}

export default EventForm;
