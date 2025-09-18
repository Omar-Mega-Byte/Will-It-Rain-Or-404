import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api/weather';

// Weather service for handling weather API calls
const weatherService = {
  /**
   * Get random weather data (no authentication required)
   * This endpoint provides real weather data from OpenWeather API for a random city
   */
  getRandomWeather: async () => {
    try {
      const response = await axios.get(`${API_BASE_URL}/random`);

      // Parse the JSON string response from backend if needed
      let weatherData = typeof response.data === 'string' ? JSON.parse(response.data) : response.data;

      // Transform the backend response to match frontend expectations
      return {
        location: {
          name: weatherData.location?.name || 'Unknown',
          country: weatherData.location?.country || 'Unknown'
        },
        current: {
          temperature: weatherData.current?.temperature || 22,
          condition: weatherData.current?.condition || 'Clear',
          feelsLike: weatherData.current?.feelsLike || weatherData.current?.temperature || 22,
          humidity: weatherData.current?.humidity || '65%',
          pressure: weatherData.current?.pressure || '1013 hPa',
          windSpeed: weatherData.current?.windSpeed || 12,
          visibility: weatherData.current?.visibility || '10 km'
        }
      };
    } catch (error) {
      console.error('Error fetching random weather:', error);
      // If backend returns an error response, pass the error message up
      if (error.response && error.response.data && typeof error.response.data === 'string') {
        try {
          const errObj = JSON.parse(error.response.data);
          return { error: errObj.error || 'Weather data unavailable.' };
        } catch (e) {
          return { error: 'Weather data unavailable.' };
        }
      }
      return { error: 'Weather data unavailable.' };
    }
  },

  /**
   * Get current weather for a specific location (requires authentication)
   */
  getCurrentWeather: async (location) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/current`, {
        params: { location }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching current weather:', error);
      throw error;
    }
  },

  /**
   * Get weather forecast (requires authentication)
   */
  getWeatherForecast: async (location, days = 7) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/forecast`, {
        params: { location, days }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching weather forecast:', error);
      throw error;
    }
  },

  /**
   * Get historical weather data (requires authentication)
   */
  getHistoricalWeather: async (location, startDate, endDate) => {
    try {
      const response = await axios.get(`${API_BASE_URL}/historical`, {
        params: { location, startDate, endDate }
      });
      return response.data;
    } catch (error) {
      console.error('Error fetching historical weather:', error);
      throw error;
    }
  }
};

export default weatherService;
