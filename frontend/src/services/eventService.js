import axios from 'axios';

const API_URL = '/api/v1/events';

class EventService {
  // ==================== CREATE OPERATIONS ====================
  
  /**
   * Create a new event
   * @param {Object} eventData - Event creation data
   * @returns {Promise<Object>} - Created event response
   */
  async createEvent(eventData) {
    try {
      const response = await axios.post(API_URL, eventData);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // ==================== READ OPERATIONS ====================
  
  /**
   * Get event by ID
   * @param {number} eventId - Event ID
   * @returns {Promise<Object>} - Event details
   */
  async getEventById(eventId) {
    try {
      const response = await axios.get(`${API_URL}/${eventId}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Get user events with pagination
   * @param {number} page - Page number (0-based)
   * @param {number} size - Number of items per page
   * @returns {Promise<Object>} - Paginated events response
   */
  async getUserEvents(page = 0, size = 10) {
    try {
      const response = await axios.get(`${API_URL}`, {
        params: { page, size }
      });
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Get all user events (no pagination)
   * @returns {Promise<Object>} - All user events
   */
  async getAllUserEvents() {
    try {
      const response = await axios.get(`${API_URL}/all`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Get upcoming events
   * @returns {Promise<Object>} - Upcoming events
   */
  async getUpcomingEvents() {
    try {
      const response = await axios.get(`${API_URL}/upcoming`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Get user event statistics
   * @returns {Promise<Object>} - Event statistics
   */
  async getUserEventStats() {
    try {
      const response = await axios.get(`${API_URL}/stats`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // ==================== UPDATE OPERATIONS ====================
  
  /**
   * Update an existing event
   * @param {number} eventId - Event ID
   * @param {Object} eventData - Updated event data
   * @returns {Promise<Object>} - Updated event response
   */
  async updateEvent(eventId, eventData) {
    try {
      const response = await axios.put(`${API_URL}/${eventId}`, eventData);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // ==================== DELETE OPERATIONS ====================
  
  /**
   * Delete an event
   * @param {number} eventId - Event ID
   * @returns {Promise<Object>} - Deletion response
   */
  async deleteEvent(eventId) {
    try {
      const response = await axios.delete(`${API_URL}/${eventId}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // ==================== SEARCH OPERATIONS ====================
  
  /**
   * Search events based on criteria
   * @param {Object} searchCriteria - Search parameters
   * @returns {Promise<Object>} - Search results
   */
  async searchEvents(searchCriteria) {
    try {
      const response = await axios.post(`${API_URL}/search`, searchCriteria);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // ==================== UTILITY OPERATIONS ====================
  
  /**
   * Check for conflicting events
   * @param {string} startDate - Start date in ISO format
   * @param {string} endDate - End date in ISO format
   * @returns {Promise<Object>} - Conflict check results
   */
  async getConflictingEvents(startDate, endDate) {
    try {
      const response = await axios.get(`${API_URL}/conflicts`, {
        params: { startDate, endDate }
      });
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  /**
   * Check if event name is available
   * @param {string} eventName - Event name to check
   * @returns {Promise<Object>} - Availability check result
   */
  async checkEventNameAvailability(eventName) {
    try {
      const response = await axios.get(`${API_URL}/check-name`, {
        params: { eventName }
      });
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  // ==================== HELPER METHODS ====================
  
  /**
   * Handle API errors consistently
   * @param {Error} error - Axios error object
   * @returns {Error} - Formatted error
   */
  handleError(error) {
    if (error.response) {
      // Server responded with error status
      const message = error.response.data?.message || 
                     error.response.data?.error || 
                     `HTTP ${error.response.status}: ${error.response.statusText}`;
      
      const customError = new Error(message);
      customError.status = error.response.status;
      customError.data = error.response.data;
      return customError;
    } else if (error.request) {
      // Network error
      return new Error('Network error. Please check your connection.');
    } else {
      // Other error
      return new Error(error.message || 'An unexpected error occurred.');
    }
  }

  // ==================== VALIDATION HELPERS ====================
  
  /**
   * Validate event data before submission
   * @param {Object} eventData - Event data to validate
   * @returns {Object} - Validation result
   */
  validateEventData(eventData) {
    const errors = {};

    if (!eventData.eventName || eventData.eventName.trim().length === 0) {
      errors.eventName = 'Event name is required';
    } else if (eventData.eventName.length > 255) {
      errors.eventName = 'Event name must not exceed 255 characters';
    }

    if (!eventData.eventDescription || eventData.eventDescription.trim().length === 0) {
      errors.eventDescription = 'Event description is required';
    } else if (eventData.eventDescription.length > 1000) {
      errors.eventDescription = 'Event description must not exceed 1000 characters';
    }

    if (!eventData.eventType) {
      errors.eventType = 'Event type is required';
    }

    if (!eventData.startDate) {
      errors.startDate = 'Start date is required';
    }

    if (eventData.startDate && eventData.endDate) {
      const start = new Date(eventData.startDate);
      const end = new Date(eventData.endDate);
      
      if (start >= end) {
        errors.endDate = 'End date must be after start date';
      }
    }

    if (eventData.startDate) {
      const start = new Date(eventData.startDate);
      const now = new Date();
      
      if (start < now) {
        errors.startDate = 'Start date cannot be in the past';
      }
    }

    if (eventData.isOutdoor === undefined || eventData.isOutdoor === null) {
      errors.isOutdoor = 'Please specify if the event is outdoor or indoor';
    }

    return {
      isValid: Object.keys(errors).length === 0,
      errors
    };
  }

  // ==================== CONSTANTS ====================
  
  /**
   * Get available event types
   * @returns {Array} - Array of event types
   */
  getEventTypes() {
    return [
      { value: 'CONCERT', label: 'Concert' },
      { value: 'CONFERENCE', label: 'Conference' },
      { value: 'MEETING', label: 'Meeting' },
      { value: 'WORKSHOP', label: 'Workshop' },
      { value: 'SPORTS', label: 'Sports' },
      { value: 'FESTIVAL', label: 'Festival' },
      { value: 'OTHER', label: 'Other' }
    ];
  }

  /**
   * Get available event statuses
   * @returns {Array} - Array of event statuses
   */
  getEventStatuses() {
    return [
      { value: 'SCHEDULED', label: 'Scheduled' },
      { value: 'IN_PROGRESS', label: 'In Progress' },
      { value: 'COMPLETED', label: 'Completed' },
      { value: 'CANCELLED', label: 'Cancelled' }
    ];
  }

  /**
   * Format date for display
   * @param {string} dateString - ISO date string
   * @returns {string} - Formatted date
   */
  formatDate(dateString) {
    if (!dateString) return '';
    
    const date = new Date(dateString);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  }

  /**
   * Format date for form input
   * @param {string} dateString - ISO date string
   * @returns {string} - Formatted date for input
   */
  formatDateForInput(dateString) {
    if (!dateString) return '';
    
    const date = new Date(dateString);
    return date.toISOString().slice(0, 16); // Format: YYYY-MM-DDTHH:MM
  }

  /**
   * Get event status color
   * @param {string} status - Event status
   * @returns {string} - CSS class for status color
   */
  getStatusColor(status) {
    const colors = {
      SCHEDULED: 'text-blue-600',
      IN_PROGRESS: 'text-yellow-600',
      COMPLETED: 'text-green-600',
      CANCELLED: 'text-red-600'
    };
    return colors[status] || 'text-gray-600';
  }

  /**
   * Get event type icon
   * @param {string} type - Event type
   * @returns {string} - Icon class or emoji
   */
  getTypeIcon(type) {
    const icons = {
      CONCERT: '🎵',
      CONFERENCE: '📊',
      MEETING: '👥',
      WORKSHOP: '🛠️',
      SPORTS: '⚽',
      FESTIVAL: '🎉',
      OTHER: '📅'
    };
    return icons[type] || '📅';
  }
}

// Export singleton instance
const eventService = new EventService();
export default eventService;
