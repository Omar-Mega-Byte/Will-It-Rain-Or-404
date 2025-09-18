import axios from 'axios';

// Base API URL - adjust according to your backend configuration
const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080';
const LOCATION_API_URL = `${API_BASE_URL}/api/v1/locations`;

// Create axios instance with default configuration
const locationAPI = axios.create({
  baseURL: LOCATION_API_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add authentication token
locationAPI.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('authToken');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response interceptor for error handling
locationAPI.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Redirect to login if unauthorized
      localStorage.removeItem('authToken');
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

/**
 * Location Service API functions
 */
const locationService = {
  /**
   * Get all locations with optional pagination and search
   * @param {Object} params - Query parameters
   * @param {number} params.page - Page number (0-based)
   * @param {number} params.size - Page size
   * @param {string} params.search - Search query
   * @param {string} params.sort - Sort criteria
   * @returns {Promise<Object>} List of locations
   */
  getAllLocations: async (params = {}) => {
    try {
      // If search is provided, use search endpoint
      if (params.search) {
        return await locationService.searchLocations(params.search);
      }

      const response = await locationAPI.get('');
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Error fetching locations:', error);
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to fetch locations'
      };
    }
  },

  /**
   * Get location by ID
   * @param {number} id - Location ID
   * @returns {Promise<Object>} Location details
   */
  getLocationById: async (id) => {
    try {
      const response = await locationAPI.get(`/${id}`);
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Error fetching location:', error);
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to fetch location'
      };
    }
  },

  /**
   * Create a new location
   * @param {Object} locationData - Location data
   * @returns {Promise<Object>} Created location
   */
  createLocation: async (locationData) => {
    try {
      const response = await locationAPI.post('', locationData);
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Error creating location:', error);
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to create location'
      };
    }
  },

  /**
   * Update an existing location
   * @param {number} id - Location ID
   * @param {Object} locationData - Updated location data
   * @returns {Promise<Object>} Updated location
   */
  updateLocation: async (id, locationData) => {
    try {
      const response = await locationAPI.put(`/${id}`, locationData);
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Error updating location:', error);
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to update location'
      };
    }
  },

  /**
   * Delete a location
   * @param {number} id - Location ID
   * @returns {Promise<Object>} Success status
   */
  deleteLocation: async (id) => {
    try {
      await locationAPI.delete(`/${id}`);
      return {
        success: true,
        message: 'Location deleted successfully'
      };
    } catch (error) {
      console.error('Error deleting location:', error);
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to delete location'
      };
    }
  },

  /**
   * Search locations by query
   * @param {string} query - Search query
   * @param {Object} options - Additional search options
   * @returns {Promise<Object>} Search results
   */
  searchLocations: async (query, options = {}) => {
    try {
      const queryParams = new URLSearchParams({ query: query });

      const response = await locationAPI.get(`/search?${queryParams.toString()}`);
      return {
        success: true,
        data: response.data
      };
    } catch (error) {
      console.error('Error searching locations:', error);
      return {
        success: false,
        error: error.response?.data?.message || 'Failed to search locations'
      };
    }
  }
};

// Export the axios instance for direct usage if needed
export { locationAPI };

export default locationService;