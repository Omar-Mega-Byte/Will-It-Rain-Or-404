import React, { useState, useEffect } from 'react';
import eventService from '../services/eventService';

const EventSearch = ({ 
  onSearch, 
  onClear, 
  isLoading = false,
  initialFilters = {}
}) => {
  const [searchFilters, setSearchFilters] = useState({
    eventName: '',
    eventType: '',
    eventStatus: '',
    isOutdoor: '',
    startDateFrom: '',
    startDateTo: '',
    endDateFrom: '',
    endDateTo: '',
    ...initialFilters
  });

  const [isExpanded, setIsExpanded] = useState(false);
  const [hasActiveFilters, setHasActiveFilters] = useState(false);

  const eventTypes = eventService.getEventTypes();
  const eventStatuses = eventService.getEventStatuses();

  useEffect(() => {
    // Check if any filters are active
    const activeFilters = Object.values(searchFilters).some(value => 
      value !== '' && value !== null && value !== undefined
    );
    setHasActiveFilters(activeFilters);
  }, [searchFilters]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setSearchFilters(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSearch = (e) => {
    e.preventDefault();
    
    // Clean up filters - remove empty values
    const cleanFilters = Object.entries(searchFilters).reduce((acc, [key, value]) => {
      if (value !== '' && value !== null && value !== undefined) {
        acc[key] = value;
      }
      return acc;
    }, {});

    onSearch(cleanFilters);
  };

  const handleClear = () => {
    const clearedFilters = Object.keys(searchFilters).reduce((acc, key) => {
      acc[key] = '';
      return acc;
    }, {});
    
    setSearchFilters(clearedFilters);
    setIsExpanded(false);
    onClear();
  };

  const handleQuickSearch = (e) => {
    const value = e.target.value;
    setSearchFilters(prev => ({
      ...prev,
      eventName: value
    }));

    // Debounced search on name only
    if (value.trim().length >= 2 || value.trim().length === 0) {
      const quickFilters = value.trim() ? { eventName: value.trim() } : {};
      onSearch(quickFilters);
    }
  };

  return (
    <div className="bg-white border border-gray-200 rounded-lg p-4 mb-6">
      {/* Quick Search Bar */}
      <div className="flex items-center space-x-4 mb-4">
        <div className="flex-1 relative">
          <div className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none">
            <svg className="h-5 w-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
            </svg>
          </div>
          <input
            type="text"
            placeholder="Search events by name..."
            value={searchFilters.eventName}
            onChange={handleQuickSearch}
            className="block w-full pl-10 pr-3 py-2 border border-gray-300 rounded-lg leading-5 bg-white placeholder-gray-500 focus:outline-none focus:placeholder-gray-400 focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
          />
        </div>

        <button
          type="button"
          onClick={() => setIsExpanded(!isExpanded)}
          className={`px-4 py-2 border rounded-lg text-sm font-medium focus:outline-none focus:ring-2 focus:ring-blue-500 ${
            isExpanded 
              ? 'bg-blue-600 text-white border-blue-600' 
              : 'bg-white text-gray-700 border-gray-300 hover:bg-gray-50'
          }`}
        >
          <div className="flex items-center space-x-2">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6V4m0 2a2 2 0 100 4m0-4a2 2 0 110 4m-6 8a2 2 0 100-4m0 4a2 2 0 100 4m0-4v2m0-6V4m6 6v10m6-2a2 2 0 100-4m0 4a2 2 0 100 4m0-4v2m0-6V4" />
            </svg>
            <span>Filters</span>
            {hasActiveFilters && (
              <span className="inline-flex items-center justify-center px-2 py-1 text-xs font-bold leading-none text-white bg-red-500 rounded-full">
                •
              </span>
            )}
          </div>
        </button>

        {hasActiveFilters && (
          <button
            type="button"
            onClick={handleClear}
            className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
          >
            Clear All
          </button>
        )}
      </div>

      {/* Advanced Filters */}
      {isExpanded && (
        <form onSubmit={handleSearch} className="space-y-4 pt-4 border-t border-gray-200">
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {/* Event Type */}
            <div>
              <label htmlFor="eventType" className="block text-sm font-medium text-gray-700 mb-1">
                Event Type
              </label>
              <select
                id="eventType"
                name="eventType"
                value={searchFilters.eventType}
                onChange={handleInputChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Types</option>
                {eventTypes.map(type => (
                  <option key={type.value} value={type.value}>
                    {eventService.getTypeIcon(type.value)} {type.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Event Status */}
            <div>
              <label htmlFor="eventStatus" className="block text-sm font-medium text-gray-700 mb-1">
                Status
              </label>
              <select
                id="eventStatus"
                name="eventStatus"
                value={searchFilters.eventStatus}
                onChange={handleInputChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Statuses</option>
                {eventStatuses.map(status => (
                  <option key={status.value} value={status.value}>
                    {status.label}
                  </option>
                ))}
              </select>
            </div>

            {/* Location Type */}
            <div>
              <label htmlFor="isOutdoor" className="block text-sm font-medium text-gray-700 mb-1">
                Location Type
              </label>
              <select
                id="isOutdoor"
                name="isOutdoor"
                value={searchFilters.isOutdoor}
                onChange={handleInputChange}
                className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
              >
                <option value="">All Locations</option>
                <option value="true">🌤️ Outdoor Only</option>
                <option value="false">🏢 Indoor Only</option>
              </select>
            </div>
          </div>

          {/* Date Range Filters */}
          <div className="space-y-4">
            <h4 className="text-sm font-medium text-gray-700">Date Range Filters</h4>
            
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {/* Start Date Range */}
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">Start Date</label>
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label htmlFor="startDateFrom" className="block text-xs text-gray-500 mb-1">From</label>
                    <input
                      type="datetime-local"
                      id="startDateFrom"
                      name="startDateFrom"
                      value={searchFilters.startDateFrom}
                      onChange={handleInputChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label htmlFor="startDateTo" className="block text-xs text-gray-500 mb-1">To</label>
                    <input
                      type="datetime-local"
                      id="startDateTo"
                      name="startDateTo"
                      value={searchFilters.startDateTo}
                      onChange={handleInputChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </div>
              </div>

              {/* End Date Range */}
              <div className="space-y-2">
                <label className="block text-sm font-medium text-gray-700">End Date</label>
                <div className="grid grid-cols-2 gap-2">
                  <div>
                    <label htmlFor="endDateFrom" className="block text-xs text-gray-500 mb-1">From</label>
                    <input
                      type="datetime-local"
                      id="endDateFrom"
                      name="endDateFrom"
                      value={searchFilters.endDateFrom}
                      onChange={handleInputChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                  <div>
                    <label htmlFor="endDateTo" className="block text-xs text-gray-500 mb-1">To</label>
                    <input
                      type="datetime-local"
                      id="endDateTo"
                      name="endDateTo"
                      value={searchFilters.endDateTo}
                      onChange={handleInputChange}
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                    />
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Quick Filter Buttons */}
          <div className="space-y-2">
            <label className="block text-sm font-medium text-gray-700">Quick Filters</label>
            <div className="flex flex-wrap gap-2">
              <button
                type="button"
                onClick={() => {
                  const today = new Date();
                  const nextWeek = new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000);
                  setSearchFilters(prev => ({
                    ...prev,
                    startDateFrom: today.toISOString().slice(0, 16),
                    startDateTo: nextWeek.toISOString().slice(0, 16)
                  }));
                }}
                className="px-3 py-1 text-sm bg-blue-100 text-blue-700 rounded-lg hover:bg-blue-200"
              >
                Next 7 Days
              </button>
              <button
                type="button"
                onClick={() => {
                  const today = new Date();
                  const nextMonth = new Date(today.getTime() + 30 * 24 * 60 * 60 * 1000);
                  setSearchFilters(prev => ({
                    ...prev,
                    startDateFrom: today.toISOString().slice(0, 16),
                    startDateTo: nextMonth.toISOString().slice(0, 16)
                  }));
                }}
                className="px-3 py-1 text-sm bg-green-100 text-green-700 rounded-lg hover:bg-green-200"
              >
                Next 30 Days
              </button>
              <button
                type="button"
                onClick={() => {
                  setSearchFilters(prev => ({
                    ...prev,
                    eventStatus: 'SCHEDULED'
                  }));
                }}
                className="px-3 py-1 text-sm bg-yellow-100 text-yellow-700 rounded-lg hover:bg-yellow-200"
              >
                Scheduled Only
              </button>
              <button
                type="button"
                onClick={() => {
                  setSearchFilters(prev => ({
                    ...prev,
                    isOutdoor: 'true'
                  }));
                }}
                className="px-3 py-1 text-sm bg-purple-100 text-purple-700 rounded-lg hover:bg-purple-200"
              >
                Outdoor Events
              </button>
            </div>
          </div>

          {/* Action Buttons */}
          <div className="flex justify-end space-x-3 pt-4 border-t border-gray-200">
            <button
              type="button"
              onClick={handleClear}
              className="px-4 py-2 text-sm font-medium text-gray-700 bg-white border border-gray-300 rounded-lg hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-blue-500"
            >
              Clear Filters
            </button>
            <button
              type="submit"
              disabled={isLoading}
              className="px-4 py-2 text-sm font-medium text-white bg-blue-600 border border-transparent rounded-lg hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {isLoading ? 'Searching...' : 'Apply Filters'}
            </button>
          </div>
        </form>
      )}

      {/* Active Filters Display */}
      {hasActiveFilters && !isExpanded && (
        <div className="pt-3 border-t border-gray-200">
          <div className="flex flex-wrap gap-2">
            <span className="text-sm text-gray-500">Active filters:</span>
            {Object.entries(searchFilters).map(([key, value]) => {
              if (!value) return null;
              
              let displayValue = value;
              if (key === 'eventType') {
                const type = eventTypes.find(t => t.value === value);
                displayValue = type ? type.label : value;
              } else if (key === 'eventStatus') {
                const status = eventStatuses.find(s => s.value === value);
                displayValue = status ? status.label : value;
              } else if (key === 'isOutdoor') {
                displayValue = value === 'true' ? 'Outdoor' : 'Indoor';
              } else if (key.includes('Date')) {
                displayValue = new Date(value).toLocaleDateString();
              }

              return (
                <span
                  key={key}
                  className="inline-flex items-center px-2 py-1 text-xs font-medium bg-blue-100 text-blue-800 rounded-full"
                >
                  {key.replace(/([A-Z])/g, ' $1').toLowerCase()}: {displayValue}
                  <button
                    onClick={() => setSearchFilters(prev => ({ ...prev, [key]: '' }))}
                    className="ml-1 inline-flex items-center justify-center w-4 h-4 text-blue-600 hover:text-blue-800"
                  >
                    ×
                  </button>
                </span>
              );
            })}
          </div>
        </div>
      )}
    </div>
  );
};

export default EventSearch;
