import React, { useState, useEffect, useCallback } from 'react';
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

    if (errors[name]) {
      setErrors(prev => ({ ...prev, [name]: '' }));
    }

    if (name === 'eventName' && value.trim() && !event) {
      debounceNameCheck(value.trim());
    }

    if ((name === 'startDate' || name === 'endDate') && value) {
      const updatedFormData = { ...formData, [name]: value };
      if (updatedFormData.startDate && updatedFormData.endDate) {
        debounceConflictCheck(updatedFormData.startDate, updatedFormData.endDate);
      }
    }
  };

  const debounceNameCheck = useCallback(
    debounce(async (name) => {
      try {
        const response = await eventService.checkEventNameAvailability(name);
        setIsNameAvailable(response.data);
      } catch (error) {
        console.error('Error checking name availability:', error);
        setErrors(prev => ({ ...prev, eventName: 'Error checking name availability' }));
      }
    }, 500),
    []
  );

  const debounceConflictCheck = useCallback(
    debounce(async (startDate, endDate) => {
      setIsCheckingConflicts(true);
      try {
        const response = await eventService.getConflictingEvents(startDate, endDate);
        const conflictingEvents = response.data || [];
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

    const submitData = {
      ...formData,
      startDate: new Date(formData.startDate).toISOString(),
      endDate: formData.endDate ? new Date(formData.endDate).toISOString() : null
    };

    try {
      await onSubmit(submitData);
    } catch (error) {
      if (error.data?.errors) {
        setErrors(error.data.errors);
      } else {
        setErrors({ form: 'Failed to submit event. Please try again.' });
      }
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6 bg-white dark:bg-gray-800 rounded-xl p-6 shadow-sm border border-gray-200 dark:border-gray-700">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Event Name */}
        <div className="md:col-span-2">
          <label htmlFor="eventName" className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1.5">
            Event Name <span className="text-red-500">*</span>
          </label>
          <div className="relative">
            <input
              type="text"
              id="eventName"
              name="eventName"
              value={formData.eventName}
              onChange={handleInputChange}
              className={`w-full px-4 py-2.5 rounded-lg border bg-gray-50 dark:bg-gray-700 dark:border-gray-600 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors ${
                errors.eventName || (!event && formData.eventName && !isNameAvailable)
                  ? 'border-red-500 focus:ring-red-500'
                  : 'border-gray-300'
              }`}
              placeholder="Enter event name"
              maxLength={255}
              aria-invalid={!!errors.eventName || (!event && formData.eventName && !isNameAvailable)}
              aria-describedby="eventName-error"
            />
            {!event && formData.eventName && (
              <span className="absolute right-3 top-1/2 -translate-y-1/2">
                {isNameAvailable ? (
                  <svg className="w-5 h-5 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                ) : (
                  <svg className="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                )}
              </span>
            )}
          </div>
          {(errors.eventName || (!event && formData.eventName && !isNameAvailable)) && (
            <p id="eventName-error" className="mt-1 text-sm text-red-600">
              {errors.eventName || 'Event name is already taken'}
            </p>
          )}
        </div>

        {/* Event Type */}
        <div>
          <label htmlFor="eventType" className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1.5">
            Event Type <span className="text-red-500">*</span>
          </label>
          <select
            id="eventType"
            name="eventType"
            value={formData.eventType}
            onChange={handleInputChange}
            className={`w-full px-4 py-2.5 rounded-lg border bg-gray-50 dark:bg-gray-700 dark:border-gray-600 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors ${
              errors.eventType ? 'border-red-500 focus:ring-red-500' : 'border-gray-300'
            }`}
            aria-invalid={!!errors.eventType}
            aria-describedby="eventType-error"
          >
            <option value="">Select event type</option>
            {eventTypes.map(type => (
              <option key={type.value} value={type.value}>
                {eventService.getTypeIcon(type.value)} {type.label}
              </option>
            ))}
          </select>
          {errors.eventType && (
            <p id="eventType-error" className="mt-1 text-sm text-red-600">{errors.eventType}</p>
          )}
        </div>

        {/* Event Status */}
        <div>
          <label htmlFor="eventStatus" className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1.5">
            Status
          </label>
          <select
            id="eventStatus"
            name="eventStatus"
            value={formData.eventStatus}
            onChange={handleInputChange}
            className="w-full px-4 py-2.5 rounded-lg border bg-gray-50 dark:bg-gray-700 dark:border-gray-600 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors border-gray-300"
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
          <label htmlFor="startDate" className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1.5">
            Start Date & Time <span className="text-red-500">*</span>
          </label>
          <input
            type="datetime-local"
            id="startDate"
            name="startDate"
            value={formData.startDate}
            onChange={handleInputChange}
            className={`w-full px-4 py-2.5 rounded-lg border bg-gray-50 dark:bg-gray-700 dark:border-gray-600 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors ${
              errors.startDate ? 'border-red-500 focus:ring-red-500' : 'border-gray-300'
            }`}
            aria-invalid={!!errors.startDate}
            aria-describedby="startDate-error"
          />
          {errors.startDate && (
            <p id="startDate-error" className="mt-1 text-sm text-red-600">{errors.startDate}</p>
          )}
        </div>

        {/* End Date */}
        <div>
          <label htmlFor="endDate" className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1.5">
            End Date & Time
          </label>
          <input
            type="datetime-local"
            id="endDate"
            name="endDate"
            value={formData.endDate}
            onChange={handleInputChange}
            className={`w-full px-4 py-2.5 rounded-lg border bg-gray-50 dark:bg-gray-700 dark:border-gray-600 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors ${
              errors.endDate ? 'border-red-500 focus:ring-red-500' : 'border-gray-300'
            }`}
            aria-invalid={!!errors.endDate}
            aria-describedby="endDate-error"
          />
          {errors.endDate && (
            <p id="endDate-error" className="mt-1 text-sm text-red-600">{errors.endDate}</p>
          )}
        </div>

        {/* Outdoor/Indoor */}
        <div className="md:col-span-2">
          <label className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1.5">
            Event Location Type <span className="text-red-500">*</span>
          </label>
          <div className="flex gap-6">
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name="isOutdoor"
                value={true}
                checked={formData.isOutdoor === true}
                onChange={() => setFormData(prev => ({ ...prev, isOutdoor: true }))}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 dark:border-gray-600"
              />
              <span className="text-sm text-gray-700 dark:text-gray-200">🌤️ Outdoor</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="radio"
                name="isOutdoor"
                value={false}
                checked={formData.isOutdoor === false}
                onChange={() => setFormData(prev => ({ ...prev, isOutdoor: false }))}
                className="h-4 w-4 text-blue-600 focus:ring-blue-500 border-gray-300 dark:border-gray-600"
              />
              <span className="text-sm text-gray-700 dark:text-gray-200">🏢 Indoor</span>
            </label>
          </div>
          {errors.isOutdoor && (
            <p className="mt-1 text-sm text-red-600">{errors.isOutdoor}</p>
          )}
        </div>

        {/* Event Description */}
        <div className="md:col-span-2">
          <label htmlFor="eventDescription" className="block text-sm font-medium text-gray-700 dark:text-gray-200 mb-1.5">
            Event Description <span className="text-red-500">*</span>
          </label>
          <textarea
            id="eventDescription"
            name="eventDescription"
            value={formData.eventDescription}
            onChange={handleInputChange}
            rows={4}
            className={`w-full px-4 py-2.5 rounded-lg border bg-gray-50 dark:bg-gray-700 dark:border-gray-600 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors resize-y ${
              errors.eventDescription ? 'border-red-500 focus:ring-red-500' : 'border-gray-300'
            }`}
            placeholder="Describe your event..."
            maxLength={1000}
            aria-invalid={!!errors.eventDescription}
            aria-describedby="eventDescription-error"
          />
          <div className="flex justify-between items-center mt-1">
            {errors.eventDescription && (
              <p id="eventDescription-error" className="text-sm text-red-600">{errors.eventDescription}</p>
            )}
            <span className="text-sm text-gray-500 dark:text-gray-400 ml-auto">
              {formData.eventDescription.length}/1000
            </span>
          </div>
        </div>
      </div>

      {/* Conflict Warning */}
      {(isCheckingConflicts || conflicts.length > 0) && (
        <div className="mt-6 p-4 bg-yellow-50 dark:bg-yellow-900/30 border border-yellow-200 dark:border-yellow-700 rounded-lg">
          <div className="flex items-start gap-3">
            <svg className="w-5 h-5 text-yellow-500 dark:text-yellow-400 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01M12 3a9 9 0 100 18 9 9 0 000-18z" />
            </svg>
            <div className="flex-1">
              {isCheckingConflicts ? (
                <p className="text-yellow-800 dark:text-yellow-200">Checking for conflicts...</p>
              ) : (
                <div>
                  <p className="text-yellow-800 dark:text-yellow-200 font-medium">
                    ⚠️ Scheduling Conflict Detected
                  </p>
                  <p className="text-yellow-700 dark:text-yellow-300 text-sm mt-1">
                    You have {conflicts.length} other event{conflicts.length > 1 ? 's' : ''} during this time:
                  </p>
                  <ul className="list-disc list-inside text-yellow-700 dark:text-yellow-300 text-sm mt-2 space-y-1">
                    {conflicts.map(conflict => (
                      <li key={conflict.id}>
                        {conflict.eventName} ({eventService.formatDate(conflict.startDate)})
                      </li>
                    ))}
                  </ul>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Form Error */}
      {errors.form && (
        <div className="mt-6 p-4 bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-700 rounded-lg">
          <p className="text-red-800 dark:text-red-200 text-sm">{errors.form}</p>
        </div>
      )}

      {/* Form Actions */}
      <div className="flex justify-end gap-3 pt-6 border-t border-gray-200 dark:border-gray-700">
        <button
          type="button"
          onClick={onCancel}
          className="px-4 py-2 text-sm font-medium text-gray-700 dark:text-gray-200 bg-gray-100 dark:bg-gray-700 border border-gray-300 dark:border-gray-600 rounded-lg hover:bg-gray-200 dark:hover:bg-gray-600 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
          disabled={isLoading}
        >
          Cancel
        </button>
        <button
          type="submit"
          disabled={isLoading || conflicts.length > 0}
          className="px-4 py-2 text-sm font-medium text-white bg-blue-600 dark:bg-blue-500 border border-transparent rounded-lg hover:bg-blue-700 dark:hover:bg-blue-600 focus:outline-none focus:ring-2 focus:ring-blue-500 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
        >
          {isLoading && <LoadingSpinner size="sm" className="text-white" />}
          {event ? 'Update Event' : 'Create Event'}
        </button>
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